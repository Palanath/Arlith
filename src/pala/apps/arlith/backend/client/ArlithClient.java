package pala.apps.arlith.backend.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.application.logging.LoggingUtilities;
import pala.apps.arlith.backend.client.api.ClientCommunity;
import pala.apps.arlith.backend.client.api.ClientOwnUser;
import pala.apps.arlith.backend.client.api.ClientThread;
import pala.apps.arlith.backend.client.api.ClientUser;
import pala.apps.arlith.backend.client.api.caching.v2.ListCache;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.client.api.notifs.ClientDirectMessageNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientFriendRequestNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientNotification;
import pala.apps.arlith.backend.client.events.EventSubsystem;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.events.StatusChangedEvent;
import pala.apps.arlith.backend.common.protocol.requests.CreateCommunityRequest;
import pala.apps.arlith.backend.common.protocol.requests.FriendByGIDRequest;
import pala.apps.arlith.backend.common.protocol.requests.FriendByNameRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetBunchOUsersRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetIncomingFriendRequestsRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetOutgoingFriendRequestsRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetOwnUserRequest;
import pala.apps.arlith.backend.common.protocol.requests.ListFriendsRequest;
import pala.apps.arlith.backend.common.protocol.requests.ListJoinedCommunitiesRequest;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.CompletableFutureUtils;
import pala.libs.generic.JavaTools;
import pala.libs.generic.events.EventHandler;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventType;

public class ArlithClient {

	/**
	 * The logger for this {@link ArlithClient}. All of the faculties that the
	 * client invokes should log to this logger. Each client and server have its own
	 * logger to help organize log output on instances running both a client and
	 * server, and to organize output against portions of the program that do not
	 * run under a client or server.
	 */
	private final Logger logger = LoggingUtilities.getConfiguredStandardLogger("CLIENT");

	/**
	 * Gets this {@link ArlithClient}'s {@link Logger}. This {@link Logger} is used
	 * to log standard, error, debug, and warning messages that occur during this
	 * client's execution. It should only be modified in certain contexts, (since
	 * the same returne d{@link Logger} is used for all operations that this
	 * {@link ArlithClient} performs), but can be printed to freely by code invoked
	 * by this client.
	 * 
	 * @return The {@link Logger} that can be used to log information for this
	 *         client.
	 */
	public Logger getLogger() {
		return logger;
	}

	private final EventManager<CommunicationProtocolEvent> eventManager = new EventManager<>();
	private final Map<GID, ClientThread> threads = new HashMap<>();
	private final LinkedHashMap<GID, ClientNotification> notifications = new LinkedHashMap<>();

	{
		eventManager.register(MessageCreatedEvent.MESSAGE_CREATED_EVENT, event -> {
			ClientThread thread = getLoadedThread(event.getMessage().getOwnerThread().getGid());
			if (thread != null)
				thread.receiveMessage(event);
			notifications.put(event.getNotificationID().getGid(),
					new ClientDirectMessageNotification(event.getNotificationID().getGid(), this,
							event.getMessage().id(), event.getMessage().getOwnerThread().getGid()));
		});
	}

	private final Map<GID, ClientUser> users = new HashMap<>();

	{
		eventManager.register(StatusChangedEvent.STATUS_CHANGED_EVENT, event -> {
			ClientUser user = getLoadedUser(event.getUser().getGid());
			if (user != null)
				user.receiveStatusChangeEvent(event);
		});
		eventManager.register(LazyProfileIconChangedEvent.LAZY_PROFILE_ICON_CHANGED_EVENT, event -> {
			ClientUser user = getLoadedUser(event.getUser().getGid());
			if (user != null)
				user.receiveLazyProfileIconChangeEvent(event);
		});
		eventManager.register(LazyCommunityImageChangedEvent.LAZY_COMMUNITY_IMAGE_CHANGED_EVENT, event -> {
			ClientCommunity community = getLoadedCommunity(event.getCommunity().getGid());
			if (community != null)
				community.receiveLazyImageChangedEvent(event);
		});
	}

	private final Map<GID, ClientCommunity> communities = new HashMap<>();
	private final ListCache<ClientCommunity> joinedCommunities;
	private final ListCache<ClientUser> friends, incomingFriends, outgoingFriends;
	private final NewCache<ClientOwnUser> self;

	private boolean running;

	private final EventSubsystem eventSubsystem;
	private final RequestQueue requestQueue;

	/**
	 * Creates an {@link ArlithClient} using the specified {@link EventSubsystem}
	 * and {@link RequestQueue}. The {@link EventSubsystem}'s {@link EventManager}
	 * is set to {@link #eventManager}. Once setup and construction is complete, the
	 * client needs to be started up with a call to {@link #startup()} before it can
	 * be used.
	 * 
	 * @param eventSubsystem   The {@link EventSubsystem} to use for listening for
	 *                         events. The {@link EventSubsystem} is expected to be
	 *                         in a non-started state. It is started up upon a call
	 *                         to {@link #startup()}.
	 * @param requestSubsystem The {@link RequestQueue} to use for making requests.
	 *                         The request subsystem is expected to be in a
	 *                         non-started state. It is started up upon a call to
	 *                         {@link #startup()}.
	 */
	public ArlithClient(EventSubsystem eventSubsystem, RequestQueue requestQueue) {
		this.eventSubsystem = eventSubsystem;
		eventSubsystem.setEventManager(eventManager);
		this.requestQueue = requestQueue;

		// Initialize caches with requestQueue.
		joinedCommunities = new ListCache<>(new ListJoinedCommunitiesRequest(), this::getCommunity, requestQueue);
		friends = new ListCache<>(new ListFriendsRequest(), a -> getUser(a.getId().getGid()), requestQueue);
		incomingFriends = new ListCache<ClientUser>(new GetIncomingFriendRequestsRequest(), a -> getUser(a.getGid()),
				requestQueue);
		outgoingFriends = new ListCache<ClientUser>(new GetOutgoingFriendRequestsRequest(), a -> getUser(a.getGid()),
				requestQueue);

		// Setup for friend events.
		eventManager.register(IncomingFriendEvent.INCOMING_FRIEND_EVENT, event -> {
			ClientUser other = getUser(event.getUser().getGid());
			REMOVE: {
				ListCache<ClientUser> cache;
				switch (event.getPreviousState()) {
				case FRIENDED:
					cache = friends;
					break;
				case INCOMING:
					cache = incomingFriends;
					break;
				default:
					break REMOVE;
				case OUTGOING:
					cache = outgoingFriends;
					break;
				}
				if (cache.isPopulated())
					try {
						cache.get().remove(other);
					} catch (CommunicationProtocolError e) {
						assert false
								: "A Cache threw a CommunicationProtocolError after it was populated. (Caches throw these errors when something goes wrong while attempting to populate. This should not happen.)";
					}
			}

			ListCache<ClientUser> cache;
			switch (event.getNewState()) {
			case FRIENDED:
				cache = friends;
				break;
			case INCOMING:
				cache = incomingFriends;
				break;
			case OUTGOING:
				cache = outgoingFriends;
				break;
			default:
				return;
			}
			if (cache.isPopulated())
				try {
					cache.get().add(other);
				} catch (CommunicationProtocolError e) {
					assert false
							: "A Cache threw a CommunicationProtocolError after it was populated. (Caches throw these errors when something goes wrong while attempting to populate. This should not happen.)";
				}
			notifications.put(event.getNotificationID().getGid(),
					new ClientFriendRequestNotification(event.getNotificationID().getGid(), this,
							event.getUser().getGid(), event.getPreviousState(), event.getNewState()));
		});

		self = new NewCache<>(new GetOwnUserRequest(), a -> {
			ClientOwnUser u = new ClientOwnUser(a.id(), ArlithClient.this, a.username(), a.status(), a.messageCount(),
					a.discriminant());
			if (!users.containsKey(a.id()))
				users.put(a.id(), u);
			return u;
		}, requestQueue);
	}

	public CompletableFuture<ClientCommunity> createCommunityRequest(String name, byte[] icon, byte[] background) {
		return getRequestQueue()
				.queueFuture(new CreateCommunityRequest(name == null ? null : new TextValue(name),
						icon == null ? null : new PieceOMediaValue(icon),
						background == null ? null : new PieceOMediaValue(background)))
				.thenApply(this::getCommunity).thenApply(t -> {
					if (joinedCommunities.isPopulated())
						// Since the cache is already populated, it will not throw a
						// CommunicationProtocolError (of any kind).
						JavaTools.hideCheckedExceptions(joinedCommunities::get).add(t);
					return t;
				});
	}

	public ClientCommunity createCommunity(String name, byte[] icon, byte[] background)
			throws SyntaxError, RuntimeException, Error, RestrictedError {
		return CompletableFutureUtils.getValue(createCommunityRequest(name, icon, background), SyntaxError.class,
				RestrictedError.class);
	}

//	/**
//	 * <p>
//	 * Creates a new Community using the provided name. The icon and background are
//	 * <i>both</i> optional. To not provide an icon or background, supply
//	 * <code>null</code> for either's {@link InputStream} argument. The sizes of the
//	 * icon and background are required to populate the fields of the respective
//	 * {@link PieceOMediaValue} objects created, but the server <b>currently does
//	 * not use</b> the size field of the {@link PieceOMediaValue}. It is recommended
//	 * to just set the values to the actual size, if known, of the media being
//	 * uploaded. If either media is not being uploaded (i.e. the
//	 * {@link pala.apps.arlith.libraries.streams.InputStream} is <code>null</code>),
//	 * then it is recommended to supply <code>-1</code> for the media size.
//	 * {@link pala.apps.arlith.libraries.streams.InputStream} is <code>null</code>),
//	 * then
//	 * </p>
//	 * 
//	 * @param name       The name of the community.
//	 * @param icon       The icon data itself, or <code>null</code> if no icon is
//	 *                   being supplied. This will be read from once this request
//	 *                   gets processed and is actually sent over the network. The
//	 *                   {@link pala.apps.arlith.libraries.streams.InputStream}
//	 *                   supplied should not be used by other code.
//	 * @param background The background data itself, or <code>null</code> if no
//	 *                   background is being supplied. This will be read from once
//	 *                   this request gets processed and is actually sent over the
//	 *                   network. The
//	 *                   {@link pala.apps.arlith.libraries.streams.InputStream}
//	 *                   should not be used by other code.
//	 * @return An {@link ActionInterface} wrapping the request.
//	 */
//	public ActionInterface<ClientCommunity> createCommunityRequest(String name, byte[] icon, byte[] background) {
//		return getRequestSubsystem().executable(a -> {
//			CommunityValue t = new CreateCommunityRequest(new TextValue(name),
//					icon == null ? null : new PieceOMediaValue(icon),
//					background == null ? null : new PieceOMediaValue(background)).inquire(a);
//			List<ClientThread> threads = new ArrayList<>();
//			for (ThreadValue th : t.getThreads())
//				threads.add(getThread(th));
//			List<GID> members = new ArrayList<>();
//			JavaTools.addAll(t.getMembers(), GIDValue::getGid, members);
//			ClientCommunity community = new ClientCommunity(t.getId().getGid(), this, t.getName().getValue(), threads,
//					members);
//			if (joinedCommunities.isPopulated())
//				joinedCommunities.poll().add(community);
//			return community;
//		});
//	}

	<T extends CommunicationProtocolEvent> void fire(EventType<T> type, T event) {
		eventManager.fire(type, event);
	}

	// TODO Add later (but make use of cache).
//	public ActionInterface<ClientUser> getUserRequest(GID id) {
//		return requestSubsystem.action(new GetUserRequest(new GIDValue(id)))
//				.then((Function<UserValue, ClientUser>) this::getUser);
//	}

	public void friend(GID userID) throws CommunicationProtocolError, RuntimeException {
		friendRequest(userID).get();
	}

	public GID friend(String user, String disc) throws CommunicationProtocolError, RuntimeException {
		return friendRequest(user, disc).get();
	}

	public ActionInterface<Void> friendRequest(GID userID) {
		return getRequestSubsystem().action(new FriendByGIDRequest(new GIDValue(userID))).then(t -> {
			if (incomingFriends.isPopulated()) {
				for (Iterator<ClientUser> iterator = incomingFriends.getUsers().iterator(); iterator.hasNext();) {
					ClientUser u = iterator.next();
					if (u.id().equals(userID)) {
						iterator.remove();
						if (friends.isPopulated())
							friends.getUsers().add(u);
						return;
					}
				}
				// Add to outgoing list, if not already present.
				if (outgoingFriends.isPopulated())
					outgoingFriends.getUsers().add(getUser(userID));
			}
		});
	}

	public ActionInterface<GID> friendRequest(String user, String disc) {
		return getRequestSubsystem().action(new FriendByNameRequest(new TextValue(user), new TextValue(disc)))
				.then((Function<GIDValue, GID>) t -> {
					if (incomingFriends.isPopulated()) {
						for (Iterator<ClientUser> iterator = incomingFriends.get().iterator(); iterator.hasNext();) {
							ClientUser u = iterator.next();
							if (u.id().equals(t.getGid())) {
								iterator.remove();
								if (friends.isPopulated())
									friends.get().add(u);
								return u.id();
							}
						}
						// Add to outgoing list, if not already present.
						if (outgoingFriends.isPopulated())
							outgoingFriends.get().add(getUser(t.getGid()));
					}
					return t.getGid();
				});
	}

//	public Set<ClientUser> getBunchOUsers(boolean filter, GID... gids)
//			throws CommunicationProtocolError,  RuntimeException {
//		return getBunchOUsers(JavaTools.iterable(gids), filter);
//	}

	public Set<ClientUser> getBunchOUsers(GID... gids) throws CommunicationProtocolError, RuntimeException {
		return getBunchOUsers(JavaTools.iterable(gids));
	}

	public Set<ClientUser> getBunchOUsers(Iterable<GID> gids) throws CommunicationProtocolError, RuntimeException {
		return getBunchOUsersRequest(gids).get();
	}

	public ActionInterface<Set<ClientUser>> getBunchOUsersRequest(GID... gids) {
		return getBunchOUsersRequest(JavaTools.iterable(gids));
	}

	/**
	 * <p>
	 * Returns a {@link List} of {@link ClientUser}s representing the users with IDs
	 * returned by the provided {@link Iterable}. The order of elements in the
	 * returned {@link Collection} (if iterated over) is not specified.
	 * </p>
	 * <p>
	 * Upon being called, this method immediately collects all the users, whose IDs
	 * are specified, that have already been loaded and are present in the cache.
	 * Then, if all the users specified have been collected, the returned
	 * {@link ActionInterface} will have already been completed successfully, in
	 * which case the {@link ActionInterface#get()} method will immediately return
	 * the {@link List} of {@link ClientUser}s. Otherwise, the returned
	 * {@link ActionInterface} queries all the users that are not already cached and
	 * then returns the list of all users requested.
	 * </p>
	 * 
	 * @param gids The {@link GID}s of the users to request.
	 * @return An {@link ActionInterface} representing the request.
	 */
	public ActionInterface<Set<ClientUser>> getBunchOUsersRequest(Iterable<GID> gids) {
		return getRequestSubsystem().executable(a -> {
			// We'll keep track of all the users we already have and the users we don't.
			Set<ClientUser> users = new HashSet<>();
			Set<GID> unknownUsers = new HashSet<>();
			synchronized (this.users) {
				// Loop over all provided GIDs and find non-loaded users.
				for (GID g : gids) {
					ClientUser user = this.users.queue(g);
					if (user != null)
						users.add(user);
					else
						unknownUsers.add(g);
				}

				// If all the users were loaded, return. Otherwise, create the action of
				// requesting the missing users from the server. After we receive those missing
				// user Communication Protocol objects, convert them all to ClientUser objects
				// (by loading them, using the constructor, then storing them in the cache), and
				// also add them to the set of users we've already loaded. Then return that set.
				if (unknownUsers.isEmpty())
					return users;
				else {
					ListValue<UserValue> t = new GetBunchOUsersRequest(
							new ListValue<>(JavaTools.mask(unknownUsers.iterator(), GIDValue::new))).inquire(a);
					for (UserValue g : t)
						this.users.put(g.id(), getUser(g));
				}
			}
			return users;
		});
	}

	public Collection<ClientThread> getCachedThreads() {
		return Collections.unmodifiableCollection(threads.values());
	}

	public Collection<ClientUser> getCachedUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	/**
	 * <p>
	 * Gets a {@link ClientCommunity} off of a {@link CommunityValue}. This is the
	 * goto conversion function from the Communication Protocol to the Application
	 * Client API regarding communities.
	 * </p>
	 * <p>
	 * This gets the specified community from the cache if it is contained in the
	 * cache. Otherwise, it creates, adds to the cache, and returns the community.
	 * This method should be used for creating and obtaining communities for cache
	 * consistency.
	 * </p>
	 * 
	 * @param c The {@link CommunityValue} to load or get the object of.
	 * @return The (possibly new) {@link ClientCommunity}.
	 */
	public ClientCommunity getCommunity(CommunityValue c) {
		ClientCommunity community;
		synchronized (communities) {
			if ((community = getLoadedCommunity(c.id())) == null)
				communities.put(c.id(), community = new ClientCommunity(c.id(), this, c.getName().getValue(),
						JavaTools.addAll(c.getThreads(), this::getThread, new ArrayList<>(c.getThreads().size())),
						JavaTools.addAll(c.getMembers(), GIDValue::getGid, new ArrayList<>(c.getMembers().size()))));
		}
		return community;
	}

	public EventSubsystem getEventSubsystem() {
		return eventSubsystem;
	}

	public List<ClientUser> getIncomingFriendRequests() throws CommunicationProtocolError, RuntimeException {
		return getIncomingFriendRequestsRequest().get();
	}

	public ActionInterface<List<ClientUser>> getIncomingFriendRequestsRequest() {
//		return incomingFriends.get
	}

	public ClientCommunity getLoadedCommunity(GID id) {
		return communities.get(id);
	}

	public ClientThread getLoadedThread(GID id) {
		return threads.get(id);
	}

	/**
	 * Gets and returns a {@link ClientUser} that this client keeps track of by the
	 * specified {@link GID}. A user becomes "tracked" if this client receives its
	 * information in an event, or if the result of a request made on this client
	 * supplies this client with its information. The client then stores it locally
	 * and it can be obtained from this method.
	 * 
	 * @param id The {@link GID} of the user to get.
	 * @return The {@link ClientUser}, or <code>null</code> if none by the given id
	 *         is tracked.
	 * @author Palanath
	 */
	public ClientUser getLoadedUser(GID id) {
		return users.get(id);
	}

	public Map<GID, ClientNotification> getNotifications() {
		return Collections.unmodifiableMap(notifications);
	}

	public List<ClientUser> getOutgoingFriendRequests() throws CommunicationProtocolError, RuntimeException {
		return getOutgoingFriendRequestsRequest().get();
	}

	public ActionInterface<List<ClientUser>> getOutgoingFriendRequestsRequest() {
		return outgoingFriends.get().then((Function<List<ClientUser>, List<ClientUser>>) Collections::unmodifiableList);
	}

	public ClientOwnUser getOwnUser() throws CommunicationProtocolError, RuntimeException {
		return getOwnUserRequest().get();
	}

	public ActionInterface<ClientOwnUser> getOwnUserRequest() {
		return self.get();
	}

	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

	public ClientThread getThread(ThreadValue thread) {
		ClientThread thd;
		synchronized (threads) {
			thd = getLoadedThread(thread.id());
			if (thd == null) {
				thd = new ClientThread(thread, this);
				threads.put(thread.id(), thd);
			} else
				thd.update(thread);
		}
		return thd;
	}

	public ClientThread getThread(GID id) {
		ClientThread thread;
		synchronized (threads) {
			thread = getLoadedThread(id);
			if (thread == null) {
				thread = new ClientThread(id, this);
				threads.put(id, thread);
			}
		}
		return thread;
	}

	/**
	 * <p>
	 * Gets a {@link ClientUser} off of a {@link UserValue}. This is the goto
	 * conversion function from the Communication Protocol to the Application Client
	 * API regarding users.
	 * </p>
	 * <p>
	 * This gets the specified user from the cache, if it is conatined in the cache,
	 * otherwise, it creates it, adds it to the cache, and returns it. This method
	 * should be used for creating and obtaining users for cache consistency.
	 * </p>
	 * 
	 * @param u The {@link UserValue} to get the user of.
	 * @return The (possibly new) {@link ClientUser}.
	 * @author Palanath
	 */
	public ClientUser getUser(UserValue u) {
		ClientUser user;
		synchronized (users) {
			user = getLoadedUser(u.id());
			if (user == null)
				users.put(u.id(), user = new ClientUser(u.id(), this, u.username(), u.status(), u.messageCount(),
						u.discriminant()));
		}
		return user;
	}

	public ClientUser getUser(GID id) {
		ClientUser user;
		synchronized (users) {
			user = getLoadedUser(id);
			if (user == null)
				users.put(id, user = new ClientUser(id, this));
		}
		return user;
	}

	public List<ClientCommunity> listJoinedCommunities() throws CommunicationProtocolError, RuntimeException {
		return listJoinedCommunitiesRequest().get();
	}

	public ActionInterface<List<ClientCommunity>> listJoinedCommunitiesRequest() {
		return joinedCommunities.get()// This get call handles synchronization and querying the server.
				.then((Function<List<ClientCommunity>, List<ClientCommunity>>) Collections::unmodifiableList);
		// Making the list unmodifiable does not need to be done in a synchronized
		// fashion.
	}

	/**
	 * Returns an unmodifiable view of the list of friends that the logged in user
	 * has.
	 * 
	 * @return An unmodifiable view of the list of users who are friended with the
	 *         logged in user.
	 * @throws CommunicationProtocolError If the list has not yet been requested
	 *                                    from the server so it is requested, and
	 *                                    that request fails with a
	 *                                    {@link CommunicationProtocolError}.
	 * @throws RuntimeException           If the list has not yet been requested
	 *                                    from the server so it is requested, and
	 *                                    that request fails with a
	 *                                    {@link RuntimeException}.
	 */
	public List<ClientUser> listFriends() throws CommunicationProtocolError, RuntimeException {
		return listFriendsRequest().get();
	}

	public ActionInterface<List<ClientUser>> listFriendsRequest() {
		return friends.get().then((Function<List<ClientUser>, List<ClientUser>>) Collections::unmodifiableList);
	}

	void reconnect() {
		stop();
		startup();
	}

	/**
	 * <p>
	 * Registers an {@link EventHandler} that will be notified of the specified type
	 * of event.
	 * </p>
	 * 
	 * @param <T>     The type of the event to handle.
	 * @param type    The {@link EventType} object representing the type of the
	 *                event to handle.
	 * @param handler The {@link EventHandler} to register, that will receive the
	 *                events for processing.
	 * @author Palanath
	 */
	public <T extends CommunicationProtocolEvent> void register(EventType<T> type, EventHandler<? super T> handler) {
		eventManager.register(type, handler);
	}

	/**
	 * Called after successful login to set up the event handler and the
	 * auto-reconnect system.
	 */
	synchronized void startup() {
		if (running)
			throw new IllegalStateException("Application Client already running!");
		running = true;
		eventSubsystem.start();
		requestQueue.start();
	}

	public synchronized void stop() {
		running = false;
		// Enter synchronization on the client.

		// While we've stolen processing, we notify other threads that a shutdown has
		// caused their stop.
		eventSubsystem.stop();
		requestQueue.stop();
	}

	public <T extends CommunicationProtocolEvent> void unregister(EventType<T> type, EventHandler<? super T> handler) {
		eventManager.unregister(type, handler);
	}

}
