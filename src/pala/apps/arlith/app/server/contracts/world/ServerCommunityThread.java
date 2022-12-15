package pala.apps.arlith.app.server.contracts.world;

public interface ServerCommunityThread extends ServerThread {
	/**
	 * <p>
	 * Deletes this {@link ServerCommunityThread}. This method does the following:
	 * </p>
	 * <ol>
	 * <li>Removes this thread from its {@link #getCommunity() owning community}'s
	 * {@link ServerCommunity#getThreads() list of community threads}.</li>
	 * <li>Deletes each of its child messages from the world.</li>
	 * <li>Deletes itself from the world.</li>
	 * </ol>
	 * <p>
	 * Note that task 2 can be accomplished through calls to
	 * {@link ServerMessage#delete()}, but such unnecessarily removes the message from
	 * this thread.
	 * </p>
	 */
	void delete();

	/**
	 * Returns the {@link ServerCommunity} that this {@link ServerCommunityThread} is
	 * contained within.
	 *
	 * @return The {@link ServerCommunity} that owns this {@link ServerCommunityThread}.
	 */
	ServerCommunity getCommunity();

	/**
	 * Returns the name of this {@link ServerCommunityThread}.
	 *
	 * @return The name of this {@link ServerCommunityThread}.
	 */
	String getName();

	/**
	 * Changes the name of this {@link ServerCommunityThread} to the specified value.
	 *
	 * @param newName The new name of this {@link ServerCommunityThread}.
	 */
	void setName(String newName);

}
