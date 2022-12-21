package pala.apps.arlith.frontend.server.world;

import java.io.File;

import pala.apps.arlith.frontend.server.contracts.coldstorage.FilesystemStorageObject;
import pala.apps.arlith.frontend.server.contracts.world.ServerMessage;
import pala.apps.arlith.frontend.server.contracts.world.ServerThread;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;
import pala.libs.generic.json.JSONObject;

class ServerMessageImpl extends ServerObjectImpl implements ServerMessage, FilesystemStorageObject {

	private static final String AUTHOR_KEY = "author", CONTENT_KEY = "content", THREAD_KEY = "thread";
	/**
	 *
	 */
	private final ServerThreadImpl thread;

	private final ServerUser author;

	private String content;

	/**
	 * <p>
	 * Creates a {@link ServerMessageImpl} with the specified owning thread, specified
	 * author, and specified contents. This constructor is designed to be called
	 * when a message is being sent in a thread.
	 * </p>
	 * <p>
	 * This constructor saves the message after it is created.
	 * </p>
	 * 
	 * 
	 * @param serverThreadImpl The thread that owns this message.
	 * @param author       The author of this message.
	 * @param content      The message's contents.
	 */
	public ServerMessageImpl(final ServerThreadImpl serverThreadImpl, final ServerUser author, final String content) {
		super(serverThreadImpl.world);
		thread = serverThreadImpl;
		this.author = author;
		this.content = content;
		save();
	}

	public ServerMessageImpl(final ServerWorldImpl world, final JSONObject snap) {
		super(snap, world);
		thread = world.getRegistry().getThread(getGID(snap, THREAD_KEY));
		author = thread.world.getRegistry().getUser(getGID(snap, AUTHOR_KEY));
		content = snap.getString(CONTENT_KEY);
	}

	/**
	 * <p>
	 * Deletes this message from its thread, and deletes the file representing it.
	 * </p>
	 * <p>
	 * This method has both <i>parent</i> effects, and <i>child</i> effects. For
	 * more details, see the {@link #deleteAsChild()} method.
	 * </p>
	 */
	@Override
	public void delete() {
		deleteAsChild();
		thread.save();// Save the parent.
	}

	/**
	 * <p>
	 * This method performs the logic of {@link #delete()} that is specific to this
	 * message. This method is typically called to delete this message <i>as a
	 * byproduct</i> of the thread that owns it being deleted, or as part of a
	 * larger operation of deleting multiple messages in the same thread.
	 * </p>
	 * <p>
	 * Normally, when {@link #delete()} is called, the message it's called on is
	 * removed from the parent thread (this is a <i>child</i> effect), and the
	 * parent thread is <b>{@link #save() saved}</b> afterwards (this is a
	 * <i>parent</i> effect). When multiple messages are being deleted in bulk
	 * through a method like {@link ServerThread#deleteMessages(java.util.Collection)},
	 * it would be wasteful, especially for large collections, for <i>parent</i>
	 * effects, such as saving the thread, to be performed <i>after each individual
	 * message is removed</i>. It would be more performant, and functionally
	 * equivalent (w.r.t. this API) to save the parent thread only once, after
	 * <i>all</i> messages in the bulk operation are deleted.
	 * </p>
	 * <p>
	 * This method exists to perform all of the message-specific deletion code.
	 * Anything that is general to the parent thread, such as saving the parent
	 * thread after a bulk operation, is manually invoked by the parent after
	 * calling this method.
	 * </p>
	 */
	void deleteAsChild() {
		thread.messages.remove(this);
		deleteFile();
	}

	/**
	 * Replaces the contents of this message with the new contents. The message is
	 * saved after.
	 */
	@Override
	public void edit(final String newContent) {
		content = newContent;
		save();
	}

	@Override
	public ServerUser getAuthor() {
		return author;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public File getStorageFile() {
		return new File(getWorld().getMessagePath(), getGID().getHex() + ".aso");
	}

	@Override
	public ServerThread getThread() {
		return thread;
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		// IMPLEMENT Auto-generated method stub
		super.restore(snap);
	}

	@Override
	public JSONObject snapshot() {
		return super.snapshot().put(AUTHOR_KEY, author.getGID().getHex()).put(CONTENT_KEY, content).put(THREAD_KEY,
				thread.getGID().getHex());
	}

	@Override
	public String toString() {
		return '[' + author.getTag() + ": " + content + ']';
	}

}