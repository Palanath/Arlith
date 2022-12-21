package pala.apps.arlith.app.server.world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pala.apps.arlith.app.server.contracts.coldstorage.FilesystemStorageObject;
import pala.apps.arlith.app.server.contracts.world.ServerMessage;
import pala.apps.arlith.app.server.contracts.world.ServerThread;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

abstract class ServerThreadImpl extends ServerObjectImpl implements ServerThread, FilesystemStorageObject {

	private static final String MESSAGES_KEY = "msgs";

	/**
	 *
	 */
	final ServerWorldImpl world;

	/**
	 * Stores all of the messages in this {@link ServerThreadImpl}. This method will
	 * probably later need to be updated so that the entire thread isn't loaded into
	 * memory at once.
	 */
	final List<ServerMessageImpl> messages = new ArrayList<>();

	@Override
	public List<? extends ServerMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public ServerThreadImpl(final ServerWorldImpl serverWorldImpl) {
		super(serverWorldImpl);
		world = serverWorldImpl;
	}

	/**
	 * Constructs this {@link ServerThreadImpl} from the provided snapshot. This
	 * constructor <i>indirectly</i> registers this {@link ServerThreadImpl} with the
	 * world registry. This constructor is to be used for data loading purposes.
	 *
	 * @param snapshot    A snapshot of this object to use to load this
	 *                    {@link ServerThreadImpl}.
	 * @param serverWorldImpl The world that the thread is a part of.
	 */
	protected ServerThreadImpl(final ServerWorldImpl serverWorldImpl, final JSONObject snapshot) {
		super(snapshot, serverWorldImpl);
		world = serverWorldImpl;

		final JSONArray msgs = getArray(snapshot, MESSAGES_KEY);
		for (final JSONValue o : msgs)
			if (o instanceof JSONString) {
				final String hex = ((JSONString) o).getValue();
				try {
					messages.add(world.getRegistry().getMessage(GID.fromHex(hex)));
				} catch (final NumberFormatException e) {
					throw new IllegalArgumentException("Invalid GID found in messages list for ServerThread.\n\tThread: "
							+ getGID() + "\n\tViolating GID: " + hex);
				}
			} else
				throw new IllegalArgumentException("Community snapshot contains invalid value in threads array: " + o);
	}

	@Override
	public boolean containsMessage(final ServerMessage message) {
//			return messages.contains(message);
		// We can't use 'message.getThread() == this', because messages that are deleted
		// from this thread will still maintain the thread they were from and return
		// that consistently from calls to `getThread()`.
		return Collections.binarySearch(messages, message, (a, b) -> a.getGID().compareTo(b.getGID())) >= 0;
	}

	/**
	 * <p>
	 * Deletes all the messages specified that are contained in this
	 * {@link ServerThreadImpl}.
	 * </p>
	 * <p>
	 * The {@link ServerThreadImpl} is saved <b>regardless of whether any messages are
	 * deleted or not</b>.
	 * </p>
	 */
	@Override
	public void deleteMessages(final Collection<? extends ServerMessage> messages) {
		for (ServerMessage m : messages)
			if (m.getThread() == this)
				((ServerMessageImpl) m).deleteAsChild();
		// In an effort to avoid a ton of binary searches, this method saves regardless
		// of
		save();
	}

	/**
	 * <p>
	 * Deletes all the messages from, and including the specified <code>from</code>
	 * index, up to, but not including, the specified <code>to</code> index.
	 * </p>
	 * <p>
	 * This message saves the thread after it's cleared if any messages were
	 * deleted.
	 * </p>
	 * 
	 * @param from The from index. Messages from this index are cleared (inclusive).
	 * @param to   The to index. Messages up to, but not including, this index are
	 *             cleared.
	 * @return Returns the number of messages that were deleted.
	 */
	private int deleteMessages(final int from, final int to) {
		if (from < 0 || to > messages.size() || from > to)
			throw new IndexOutOfBoundsException();
		if (to - from > 0) {
			for (int i = to; i > from; i--)
				messages.get(from).deleteAsChild();
			save();
		}
		return to - from;
	}

	@Override
	public int deleteMessagesAfter(final ServerMessage pivot, final int amount) throws IllegalArgumentException {
		final int ind = indexOf(pivot);
		if (ind == -1)
			throw new IllegalArgumentException("The provided message is not contained in this thread.");
		if (ind == messages.size() - 1)
			return 0;
		return deleteMessages(ind + 1, Math.min(messages.size(), ind + 1 + amount));
	}

	@Override
	public int deleteMessagesBefore(final ServerMessage pivot, final int amount) throws IllegalArgumentException {
		final int ind = indexOf(pivot);
		if (ind == -1)
			throw new IllegalArgumentException("The provided message is not contained in this thread.");
		if (ind == 0)
			return 0;
		return deleteMessages(Math.max(0, ind - amount), ind);
	}

	@Override
	public void deleteMessagesByID(final Collection<GID> messages) {
		// TODO Optimize using order of threads' messages.
		boolean deletedAny = false;

		for (int i = 0; i < this.messages.size(); i++) {
			ServerMessageImpl m = this.messages.get(i);
			if (messages.contains(m.getGID())) {
				m.deleteAsChild();
				deletedAny = true;
				i--;
			}
		}
		if (deletedAny)
			save();
	}

	@Override
	public List<? extends ServerMessage> getEarliestMessages(final int count) {
		return new ArrayList<>(messages.subList(0, Math.min(count, messages.size())));
	}

	@Override
	public List<? extends ServerMessage> getLatestMessages(final int count) {
		return new ArrayList<>(messages.subList(Math.max(0, messages.size() - count), messages.size()));
	}

	@Override
	public List<? extends ServerMessage> getPreviousMessages(final ServerMessage pivot, final int count)
			throws IllegalArgumentException {
		final int ind = indexOf(pivot);
		if (ind == -1)
			throw new IllegalArgumentException("The provided message is not contained in this thread.");
		// Get messages before pivot index. Same as #getLatestMessages, but with pivot
		// index instead of messages.size().
		return new ArrayList<>(messages.subList(Math.max(0, ind - count), ind));
	}

	@Override
	public List<? extends ServerMessage> getSubsequentMessages(final ServerMessage pivot, final int count)
			throws IllegalArgumentException {
		final int ind = indexOf(pivot);
		if (ind == -1)
			throw new IllegalArgumentException("The provided message is not contained in this thread.");
		// Get messages after pivot index.
		// [pivot-ind, Math.min(messages.size(), pivot-ind + count)]
		return new ArrayList<>(messages.subList(ind + 1, Math.min(messages.size(), ind + count + 1)));
	}

	private int indexOf(final ServerMessage message) {
		return messages.indexOf(message);
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		// IMPLEMENT Auto-generated method stub
		super.restore(snap);
	}

	@Override
	public ServerMessage sendMessage(final String text, final ServerUser author) throws IllegalArgumentException {
		if (!isParticipant(author))
			throw new IllegalArgumentException("Specified message author is not a member of this thread.");
		ServerMessageImpl messageImpl = new ServerMessageImpl(this, author, text);
		messages.add(messageImpl);
		save();
		return messageImpl;
	}

	@Override
	public JSONObject snapshot() {
		final JSONObject snap = super.snapshot();
		snap.put(MESSAGES_KEY, new JSONArray(JavaTools.mask(messages, a -> new JSONString(a.getGID().getHex()))));
		return snap;
	}

	@Override
	public String toString() {
		return "Thread[members=" + getParticipants() + ']';
	}

}