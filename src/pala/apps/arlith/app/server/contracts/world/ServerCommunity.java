package pala.apps.arlith.app.server.contracts.world;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.api.streams.InputStream;
import pala.apps.arlith.app.server.contracts.media.MediaUpload;

public interface ServerCommunity extends ServerObject {

	/**
	 * Adds a {@link ServerUser} to this {@link ServerCommunity}.
	 *
	 * @param user The {@link ServerUser}.
	 */
	void addUser(ServerUser user);

	/**
	 * <p>
	 * Changes the owner of this {@link ServerCommunity} to the specified
	 * {@link ServerUser}.
	 * </p>
	 * <p>
	 * If the specified {@link ServerUser} is not a member of this {@link ServerCommunity},
	 * this method does nothing. A user must already be inside the
	 * {@link ServerCommunity} to be able to receive ownership. Additionally, if the
	 * specified user already owns the community, this method does nothing.
	 * </p>
	 *
	 * @param newOwner The new owner.
	 */
	void changeOwner(ServerUser newOwner);

	/**
	 * Checks if the specified {@link ServerMessage} is contained within any of the
	 * {@link ServerThread}s in this {@link ServerCommunity}.
	 *
	 * @param message The {@link ServerMessage} to check for the presence of.
	 * @return <code>true</code> if the specified {@link ServerMessage} is contained in
	 *         this {@link ServerCommunity}. <code>false</code> otherwise.
	 */
	default boolean containsMessage(final ServerMessage message) {
		for (final ServerThread t : getThreads())
			if (t.containsMessage(message))
				return true;
		return false;
	}

	/**
	 * Checks if the specified thread is contained in this {@link ServerCommunity}.
	 *
	 * @param thread The ServerThread to check for the presence of.
	 * @return <code>true</code> if the thread is contained in this
	 *         {@link ServerCommunity}. <code>false</code> otherwise.
	 */
	default boolean containsThread(final ServerCommunityThread thread) {
		for (final ServerCommunityThread t : getThreads())
			if (t.equals(thread))
				return true;
		return false;
	}

	/**
	 * Checks if the specified user is contained in this {@link ServerCommunity}.
	 *
	 * @param user The {@link ServerUser} to check for the presence of.
	 * @return <code>true</code> if the specified {@link ServerUser} is contained in
	 *         this {@link ServerCommunity}. <code>false</code> otherwise.
	 */
	default boolean containsUser(final ServerUser user) {
		for (final ServerUser u : getUsers())
			if (u.equals(user))
				return true;
		return false;
	}

	/**
	 * Creates a {@link ServerThread} inside this {@link ServerCommunity} and returns it.
	 * The index of the newly created {@link ServerThread} will be equal to the
	 * <b>new</b> number of {@link ServerThread}s in the community minus <code>1</code>;
	 * the {@link ServerThread} will be at the bottom of the list of threads in the
	 * {@link ServerCommunity}.
	 *
	 * @param name The name of the {@link ServerThread}.
	 * @return The newly created {@link ServerThread}.
	 */
	default ServerCommunityThread createThread(final String name) {
		final List<? extends ServerCommunityThread> threads = getThreads();
		return threads.isEmpty() ? createThreadAtIndex(name, 0)
				: createThreadBelow(name, threads.get(threads.size() - 1));
	}

	/**
	 * <p>
	 * Creates a thread immediately before the specified target thread. The newly
	 * created thread gets the index of the target thread and the indices of the
	 * target thread and every thread after it are moved by <code>1</code>.
	 * </p>
	 * <p>
	 * If the target thread is not contained within this {@link ServerCommunity}, an
	 * {@link IllegalArgumentException} is thrown.
	 * </p>
	 *
	 * @param name   The name of the thread that will be created.
	 * @param target The target thread that the new thread will be created above.
	 * @return The newly created thread.
	 * @throws IllegalArgumentException If the target thread is not in this
	 *                                  {@link ServerCommunity}.
	 */
	default ServerCommunityThread createThreadAbove(final String name, final ServerCommunityThread target)
			throws IllegalArgumentException {
		final int ind = indexOf(target);
		if (ind == -1)
			throw new IllegalArgumentException("Provided target thread is not contained within this community.");
		return createThreadAtIndex(name, ind);
	}

	/**
	 * <p>
	 * Creates a new {@link ServerThread} with the specified name at the specified index
	 * in this {@link ServerCommunity}.
	 * </p>
	 * <p>
	 * If the index is less than <code>0</code> or greater than
	 * {@link #getThreads()}<code>.</code>{@link Collection#size() size()}, this
	 * method throws an {@link IndexOutOfBoundsException}.
	 * </p>
	 *
	 * @param name  The name of the new thread.
	 * @param index The index of the new thread.
	 * @return The newly created {@link ServerThread}.
	 * @throws IndexOutOfBoundsException If the provided index is greater than the
	 *                                   number of threads in this
	 *                                   {@link ServerCommunity} or less than
	 *                                   <code>0</code>.
	 */
	ServerCommunityThread createThreadAtIndex(String name, int index) throws IndexOutOfBoundsException;

	/**
	 * <p>
	 * Creates a thread immediately after the specified target thread. If there were
	 * already any elements after the target thread, their indices are incremented.
	 * </p>
	 * <p>
	 * If the target thread is not contained in this {@link ServerCommunity}, this
	 * method throws an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param name   The name of the thread to create.
	 * @param target The target thread that the new thread will be created below.
	 * @return The newly created thread.
	 * @throws IllegalArgumentException If the target thread is not in this
	 *                                  {@link ServerCommunity}.
	 */
	default ServerCommunityThread createThreadBelow(final String name, final ServerCommunityThread target)
			throws IllegalArgumentException {
		final int ind = indexOf(target);
		if (ind == -1)
			throw new IllegalArgumentException("Provided target thread is not contained within this community.");
		return createThreadAtIndex(name, ind + 1);
	}

	/**
	 * <p>
	 * Deletes this {@link ServerCommunity}. This method does the following:
	 * </p>
	 * <ol>
	 * <li>Removes this community from the {@link ServerUser#getJoinedCommunities()}
	 * list of each of its members.</li>
	 * <li>Deletes each of its child threads from the world.</li>
	 * <li>Deletes itself from the world.</li>
	 * </ol>
	 * <p>
	 * Note that the second task can be accomplished through a call to
	 * {@link ServerCommunityThread#delete()}, however that also attempts to remove the
	 * thread from this community object, which is unnecessary.
	 * </p>
	 */
	void delete();

	/**
	 * <p>
	 * Returns an {@link InputStream} that provides the background of this
	 * {@link ServerCommunity}, or <code>null</code>, if no background exists.
	 * </p>
	 * <p>
	 * Note that the {@link InputStream} returned <span style="color: red;">may lock
	 * the hard-disk/file-system object that stores the profile icon</span> and may
	 * thusly prevent the {@link #setBackground(byte[]) corresponding setter} method
	 * from operating correctly. (Usually this manifests, on Windows, as the setter
	 * not being able to remove the icon for some time; specifically until the
	 * garbage collector has collected the {@link InputStream}.) The
	 * {@link InputStream} should be closed once its use is completed (as soon as
	 * possible) to make sure that the setter method does not fail unexpectedly.
	 * </p>
	 * 
	 * @return An {@link InputStream} that can be read to obtain the bytes of the
	 *         background of this {@link ServerCommunity} or <code>null</code>.
	 * @throws IOException If an {@link IOException} occurs while trying to open the
	 *                     {@link InputStream}.
	 */
	InputStream getBackground() throws IOException;

	/**
	 * <p>
	 * Returns an {@link InputStream} that provides the icon of this
	 * {@link ServerCommunity}, or <code>null</code>, if no icon exists.
	 * </p>
	 * <p>
	 * Note that the {@link InputStream} returned <span style="color: red;">may lock
	 * the hard-disk/file-system object that stores the profile icon</span> and may
	 * thusly prevent the {@link #setIcon(byte[]) corresponding setter} method from
	 * operating correctly. (Usually this manifests, on Windows, as the setter not
	 * being able to remove the icon for some time; specifically until the garbage
	 * collector has collected the {@link InputStream}.) The {@link InputStream}
	 * should be closed once its use is completed (as soon as possible) to make sure
	 * that the setter method does not fail unexpectedly.
	 * </p>
	 * 
	 * @return An {@link InputStream} that can be read to obtain the bytes of the
	 *         icon of this {@link ServerCommunity} or <code>null</code>.
	 * @throws IOException If an {@link IOException} occurs while trying to open the
	 *                     {@link InputStream}.
	 */
	InputStream getIcon() throws IOException;

	/**
	 * Returns the name of this {@link ServerCommunity}.
	 *
	 * @return The name of this {@link ServerCommunity}.
	 */
	String getName();

	/**
	 * Gets the {@link ServerUser} that owns this {@link ServerCommunity}.
	 *
	 * @return The {@link ServerUser} that owns this {@link ServerCommunity}.
	 */
	ServerUser getOwner();

	/**
	 * Returns an immutable {@link List} of the {@link ServerThread}s in this
	 * {@link ServerCommunity}, in correct order.
	 *
	 * @return An immutable {@link List} of the {@link ServerThread}s in this
	 *         {@link ServerCommunity}.
	 */
	List<? extends ServerCommunityThread> getThreads();

	/**
	 * Returns an immutable {@link List} of the {@link ServerUser}s in this
	 * {@link ServerCommunity}.
	 *
	 * @return An immutable {@link List} of the {@link ServerUser}s in this
	 *         {@link ServerCommunity}.
	 */
	Collection<? extends ServerUser> getUsers();

	/**
	 * Returns the index of the specified thread in this {@link ServerCommunity}. If the
	 * {@link ServerThread} is not in this community, this method returns
	 * <code>-1</code>.
	 *
	 * @param thread The {@link ServerThread}.
	 * @return The index of the thread or <code>-1</code> if the thread is not in
	 *         this community.
	 */
	default int indexOf(final ServerCommunityThread thread) {
		int i = 0;
		for (final Iterator<? extends ServerCommunityThread> iterator = getThreads().iterator(); iterator.hasNext(); i++)
			if (iterator.next() == thread)
				return i;
		return -1;
	}

	/**
	 * Removes the specified {@link ServerThread} from this {@link ServerCommunity}. This
	 * deletes the thread entirely. If the {@link ServerThread} is not a member of this
	 * {@link ServerCommunity}, this method does nothing.
	 *
	 * @param thread The {@link ServerThread} to remove from this {@link ServerCommunity}.
	 */
	void removeThread(ServerCommunityThread thread);

	/**
	 * Removes a {@link ServerUser} from this {@link ServerCommunity}. If the {@link ServerUser}
	 * is not in this community, this method does nothing. If the {@link ServerUser} is
	 * the owner of the {@link ServerCommunity}, this method does nothing.
	 * ({@link #delete()} should be used to delete the server if the owner needs to
	 * leave it, or ownership should be transferred via {@link #changeOwner(ServerUser)}
	 * before the owner can leave.)
	 *
	 * @param user The {@link ServerUser} to remove.
	 */
	void removeUser(ServerUser user);

	/**
	 * Renames this {@link ServerCommunity}.
	 *
	 * @param newName The new name of the ServerCommunity.
	 */
	void rename(String newName);

	/**
	 * Sets the background of this {@link ServerCommunity} to be the provided
	 * {@link MediaUpload}. If the provided icon argument is <code>null</code>, the
	 * background image is removed. Implementations should throw an error if removal
	 * fails, so that the corresponding {@link #getBackground() getter} will not
	 * silently return the purportedly returned image when subsequently called.
	 *
	 * @param icon The {@link MediaUpload}.
	 * @throws IOException               If an {@link IOException} occurs while
	 *                                   opening the file to write to or while
	 *                                   reading from the media upload/writing to
	 *                                   the file.
	 * @throws FileNotFoundException     If a {@link FileNotFoundException} occurs
	 *                                   while opening the file for writing.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs.
	 * @throws BlockException            If a {@link BlockException} occurs.
	 */
	void setBackground(byte[] icon)
			throws FileNotFoundException, IOException, UnknownCommStateException, BlockException;

	/**
	 * Sets the icon of this {@link ServerCommunity} to the provided {@link MediaUpload}
	 * object. If the provided icon argument is <code>null</code>, the icon image is
	 * removed. Implementations should throw an error if removal fails, so that the
	 * corresponding {@link #getIcon() getter} will not silently return the
	 * purportedly returned image when subsequently called.
	 *
	 * @param icon The {@link MediaUpload} object.
	 * @throws IOException               If an {@link IOException} occurs while
	 *                                   opening the file to write to or while
	 *                                   reading from the media upload/writing to
	 *                                   the file.
	 * @throws FileNotFoundException     If a {@link FileNotFoundException} occurs
	 *                                   while opening the file for writing.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs.
	 * @throws BlockException            If a {@link BlockException} occurs.
	 */
	void setIcon(byte[] icon) throws FileNotFoundException, IOException, UnknownCommStateException, BlockException;
}
