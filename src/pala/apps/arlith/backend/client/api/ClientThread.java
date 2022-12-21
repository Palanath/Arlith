package pala.apps.arlith.backend.client.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.Cache;
import pala.apps.arlith.backend.client.api.caching.ClientCache;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.requests.GetThreadMembersRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetThreadRequest;
import pala.apps.arlith.backend.common.protocol.requests.RetrieveMessagesBeforeRequest;
import pala.apps.arlith.backend.common.protocol.requests.RetrieveMessagesRequest;
import pala.apps.arlith.backend.common.protocol.requests.SendMessageRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.IntegerValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.JavaTools;
import pala.libs.generic.generators.Generator;
import pala.libs.generic.generators.NullstopGenerator;

/**
 * <p>
 * Represents a thread of messages. For general indexing purposes in this class,
 * the 0th message represents the first message sent in this thread, (so, the
 * "earliest" message sent). This means that, as new messages are sent in this
 * thread, they are inserted at the end of this thread's history (at position
 * {@link #loadedSize()} in the thread's backing list).
 * </p>
 * <p>
 * All changes to this {@link ClientThread} object by the client's internal
 * event handlers are done with a synchronization lock on this
 * {@link ClientThread} object. For purposes of thread synchronization with the
 * event thread that automatically updates this {@link ClientThread} object,
 * locking on this object is sufficient.
 * </p>
 * 
 * @author Palanath
 *
 */
public class ClientThread extends SimpleClientObject implements Named {
	public ClientThread(GID gid, ArlithClient client) {
		super(gid, client);
	}

	public ClientThread(ThreadValue thread, ArlithClient client) {
		this(thread.id(), client);
		this.name.populate(thread.name());
	}

	private final Cache<String> name = new ClientCache<String>(() -> client().getRequestSubsystem()) {

		@Override
		protected String queryFromServer(CommunicationConnection connection)
				throws CommunicationProtocolError, RuntimeException {
			return new GetThreadRequest(new GIDValue(id())).inquire(connection).name();
		}
	};
//	private final Variable<String> name = new Variable<>();
	private static final int REQUEST_BATCH_SIZE = 50;
	private final List<ClientMessage> messages = Collections.synchronizedList(new ArrayList<>());
	private final Cache<List<ClientUser>> members = new ClientCache<List<ClientUser>>(
			() -> client().getRequestSubsystem()) {

		@Override
		protected List<ClientUser> queryFromServer(CommunicationConnection connection)
				throws CommunicationProtocolError, RuntimeException {
			return JavaTools.addAll(new GetThreadMembersRequest(new GIDValue(id())).inquire(connection),
					client()::getUser, new ArrayList<>());
		}
	};

	private volatile boolean reachedEnd;

//	/**
//	 * Queries all the properties of this object from Application servers (if
//	 * necessary) and puts them into cache (into the fields of this class).
//	 * Subsequently calls the locality checker to obtain the value from cache.
//	 * Querying is not necessary when the locality checker does not initially return
//	 * <code>null</code>.
//	 * 
//	 * @param <R>             The type of the result property that is desired.
//	 * @param localityChecker The local cache checker (checks the fields of this
//	 *                        object) for the requested property.
//	 * @return A {@link RequestAction} representing the possible query and property
//	 *         request.
//	 * @author Palanath
//	 */
//	private <R> RequestAction<GetThreadRequest, ThreadValue, R> getProperties(Function<ClientThread, R> localityChecker) {
//		return client().getRequestSubsystem().wrap(new GetThreadRequest(new GIDValue(id())), t -> {
//			name = t.name();
//			return localityChecker.apply(this);
//		}, () -> localityChecker.apply(this));
//	}

	public synchronized List<ClientUser> getMembers() throws CommunicationProtocolError, RuntimeException {
		return getMembersRequest().get();
	}

	public synchronized ActionInterface<List<ClientUser>> getMembersRequest() {
		return members.get().then((Function<List<ClientUser>, List<ClientUser>>) Collections::unmodifiableList);
	}

	public synchronized ClientMessage getMessageFromMessageValue(MessageValue msg) {
		if (!id().equals(msg.getOwnerThread().getGid()))
			throw new IllegalArgumentException(
					"You must provide a MessageValue that represents a message which belongs to this thread.");
		int index = Collections.binarySearch(messages, msg,
				(o1, o2) -> (o1 instanceof ClientMessage ? ((ClientMessage) o1).id() : ((MessageValue) o1).id())
						.compareTo(o2 instanceof ClientMessage ? ((ClientMessage) o2).id() : ((MessageValue) o2).id()));
		if ((index & 0x80000000) == 0)
			return messages.get(index);
		else {
			ClientMessage message = new ClientMessage(msg, client());
			messages.add(-index, message);
			return message;
		}

	}

	// Although the following two messages do not return CompletableFutures or have
	// "request" counterparts, they still contact the server and make a request.

	public synchronized List<ClientMessage> getLatest(int messages) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, ObjectNotFoundError, AccessDeniedError, RuntimeException {
		return getRange(0, messages);
	}

	public synchronized List<ClientMessage> getRange(int from, int to) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, ObjectNotFoundError, AccessDeniedError, RuntimeException {
		int total = messages.size();
		if (from > to)
			throw new IllegalArgumentException("Invalid range.");
		else if (from == to)
			return Collections.emptyList();
		else if (to > total)
			requestEarlier(to - total);
		if (to > total)
			to = total;
		if (from < 0)
			from = 0;

		int realFrom = total - to, realTo = total - from;

		ArrayList<ClientMessage> res = new ArrayList<>(messages.subList(realFrom, realTo));
		Collections.reverse(res);
		return res;
	}

	public int loadedSize() {
		return messages.size();
	}

	/**
	 * <p>
	 * Returns a {@link Generator} that supplies the caller with messages in the
	 * order of this thread's history, from the latest message sent (bottom of the
	 * history) to the earliest message sent (top of the history).
	 * </p>
	 * <p>
	 * <b>Note</b> that it is highly advised that iteration over this thread's
	 * history be done in a synchronized fashion, where a lock on this
	 * {@link ClientThread} is obtained. Failure to do so can allow other threads to
	 * update this {@link ClientThread} while the generator is unfinished, which can
	 * cause unexpected behavior
	 * </p>
	 * <p>
	 * The returned generator signifies that the end has been reached by returning
	 * <code>null</code>. The returned generator will also request messages as
	 * needed to continue supplying them once the end of the cached history is
	 * reached.
	 * </p>
	 * 
	 * @return A fresh new {@link NullstopGenerator}.
	 * @throws RuntimeException If a {@link CommunicationProtocolError} occurs
	 *                          during any call to {@link Generator#next()} or
	 *                          {@link NullstopGenerator#hasNext()}.
	 * @author Palanath
	 */
	public NullstopGenerator<ClientMessage> history() {
		return new NullstopGenerator<ClientMessage>() {
			int i = messages.size() - 1;
			boolean hitTop;

			@Override
			protected ClientMessage nextItem() {
				synchronized (ClientThread.this) {
					if (i < 0 && !hitTop) {
						try {
							i += requestEarlier(REQUEST_BATCH_SIZE);
						} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError
								| AccessDeniedError e) {
							throw new RuntimeException(e);
						}
						if (i < 0)
							hitTop = true;
					}
					return i < 0 ? null : messages.get(i--);
				}
			}
		};
	}

	public ClientMessage earliestCached() {
		return messages.isEmpty() ? null : messages.get(0);
	}

	public ClientMessage latestCached() {
		return messages.isEmpty() ? null : messages.get(messages.size() - 1);
	}

	public List<ClientMessage> cacheView() {
		return Collections.unmodifiableList(messages);
	}

	public int cacheSize() {
		return messages.size();
	}

	public List<ClientMessage> cacheView(int from, int to) {
		return Collections.unmodifiableList(messages.subList(from, to));
	}

	private int requestEarlier(int messages) throws SyntaxError, RateLimitError, ServerError, RestrictedError,
			ObjectNotFoundError, AccessDeniedError, RuntimeException {
		if (reachedEnd)
			return 0;
		ListValue<MessageValue> res;
		try {
			if (this.messages.isEmpty())
				res = client().getRequestSubsystem()
						.action(new RetrieveMessagesRequest(new GIDValue(id()), new IntegerValue(messages))).get();
			else
				res = client().getRequestSubsystem().action(new RetrieveMessagesBeforeRequest(new GIDValue(id()),
						new IntegerValue(messages), new GIDValue(this.messages.get(0).id()))).get();
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError
				| AccessDeniedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
		List<ClientMessage> msg = new ArrayList<>(res.size());
		for (MessageValue a : res)
			msg.add(new ClientMessage(a.getId().getGid(), a.content(), a.getSenderUser().getGid(),
					a.getOwnerThread().getGid(), client()));
		this.messages.addAll(0, msg);
		if (res.size() < messages)
			reachedEnd = true;
		return res.size();
	}

	public ActionInterface<ClientMessage> sendMessageRequest(String text) {
		return client().getRequestSubsystem().action(new SendMessageRequest(new GIDValue(id()), new TextValue(text)))
				.then((Function<MessageValue, ClientMessage>) a -> {
					ClientMessage message = new ClientMessage(a, client());
					synchronized (this) {
						messages.add(message);
					}
					return message;
				});
	}

	public ClientMessage sendMessage(String text) throws CommunicationProtocolError, RuntimeException {
		return sendMessageRequest(text).get();
	}

	/**
	 * <p>
	 * Called by the {@link ArlithClient} that owns this {@link ClientThread} when notice
	 * that a new message has been made in this thread is received from the server
	 * by the client. This method adds that message to this {@link ClientThread}.
	 * </p>
	 * <p>
	 * Please note that no check is made to ensure that the provided event
	 * represents a message creation of a message that actually was made in the this
	 * thread.
	 * </p>
	 * 
	 * @param event The event that the server sent which notes this newly created
	 *              message.
	 * @return The created {@link ClientMessage}.
	 */
	public synchronized ClientMessage receiveMessage(MessageCreatedEvent event) {
		ClientMessage msg = new ClientMessage(event.getMessage(), client());
		messages.add(msg);
		return msg;
	}

	/**
	 * The same as {@link #getName()} but swallows errors with a
	 * {@link RuntimeException}.
	 */
	@Override
	public String name() throws RuntimeException {
		try {
			return getName();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ActionInterface<String> getNameRequest() {
		return name.get();
	}

	public String getName() throws CommunicationProtocolError, RuntimeException {
		return getNameRequest().get();
	}

	/**
	 * <p>
	 * Gets a {@link ClientMessage} by the given {@link GID}. This method returns
	 * the {@link ClientMessage} that has the provided {@link GID}.
	 * </p>
	 * <p>
	 * This method will compare the timestamp of the provided message ID with the
	 * earliest message in the cache. If the provided ID's timestamp is earlier than
	 * that of the earliest message in this cache, messages are requested in batches
	 * of {@link #REQUEST_BATCH_SIZE} until either the first message in this thread
	 * is acquired or a message sent earlier than the timestamp of the provided
	 * {@link GID} is acquired. In either case, the
	 * 
	 * @param message The {@link GID} of the message to get.
	 * @return The gotten message, or <code>null</code> if the provided message is
	 *         not in this {@link ClientThread}.
	 * @throws AccessDeniedError   If the calling client does not have access to
	 *                             perform the specified action because of missing
	 *                             permissions. (E.g., the caller does not have
	 *                             access to the thread represented by this
	 *                             {@link ClientThread}.)
	 * @throws ObjectNotFoundError If the thread that this {@link ClientThread}
	 *                             could not be found on the server. (Specifically,
	 *                             if there is no thread known to the server that
	 *                             has the same GID as this object.)
	 * @throws RestrictedError     If the client is in a non-logged-in state and
	 *                             therefore does not have permission to perform
	 *                             this action.
	 * @throws ServerError         If there is an unforeseen server error.
	 * @throws RateLimitError      If the client is being rate limited.
	 * @throws SyntaxError         If the server believes that there is a syntax
	 *                             error in the request that invokes the action that
	 *                             this method represents.
	 * @throws RuntimeException    If some, arbitrary {@link RuntimeException}
	 *                             occurs while requesting earlier messages.
	 *                             connection.
	 */
	public ClientMessage getMessage(GID message) throws SyntaxError, RateLimitError, ServerError, RestrictedError,
			ObjectNotFoundError, AccessDeniedError, RuntimeException {
		ClientMessage earliest = earliestCached();
		List<ClientMessage> messages = this.messages;
		if (earliest == null || message.compareTo(earliest.id()) < 0) {
			int i;
			do
				if ((i = requestEarlier(REQUEST_BATCH_SIZE)) == 0)
					return null;
				else if (i < REQUEST_BATCH_SIZE)
					if (message.compareTo(earliestCached().id()) < 0)
						return null;
					else
						break;
			while (message.compareTo(earliestCached().id()) < 0);
			messages = messages.subList(messages.size() - i, messages.size());
		}
		int i = Collections.binarySearch(messages, message, (o1, o2) -> {
			if (!(o1 instanceof GID))
				o1 = ((ClientMessage) o1).id();
			if (!(o2 instanceof GID))
				o2 = ((ClientMessage) o2).id();
			return ((GID) o1).compareTo((GID) o2);
		});
		return i < 0 ? null : messages.get(i);

	}

	/**
	 * <p>
	 * It may be the case that this thread was loaded into the client at some point,
	 * but not all of its properties (i.e., its name) were queried from the server
	 * yet. This is actually the usual way that most threads are retrieved. However,
	 * in some cases, these {@link ClientThread} objects may already exist in a
	 * {@link ArlithClient} but the {@link ArlithClient} may later receive a {@link ThreadValue}
	 * representing these same, loaded threads from the server. In this case, the
	 * threads' names (and other properties, if any more), may not have been set in
	 * the {@link ClientThread} objects but have been received from the server in
	 * the {@link ThreadValue} objects. This method is called by the client in this
	 * case to set those received properties, so that the client doesn't have to
	 * make another request if it needs the already received information later on.
	 * </p>
	 * <p>
	 * This method simply checks if {@link #name} has been set. If it hasn't, then
	 * this method sets it based off of the value given in the provided
	 * {@link ThreadValue}. There are currently no other properties that a
	 * {@link ClientThread} possesses that need to be updated in this way.
	 * </p>
	 * 
	 * @param thread The {@link ThreadValue} to update from.
	 */
	public void update(ThreadValue thread) {
		if (!name.isPopulated())
			name.populate(thread.name());
	}

}
