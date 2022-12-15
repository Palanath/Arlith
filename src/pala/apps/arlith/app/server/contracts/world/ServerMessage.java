package pala.apps.arlith.app.server.contracts.world;

public interface ServerMessage extends ServerObject {
	/**
	 * <p>
	 * Deletes this {@link ServerMessage}. This method does the following:
	 * </p>
	 * <ol>
	 * <li>Removes itself from its {@link #getThread() parent thread}.</li>
	 * <li>Deletes itself from the world.</li>
	 * </ol>
	 */
	void delete();

	/**
	 * Edits this {@link ServerMessage message} to contain the specified content.
	 *
	 * @param newContent The new content of this {@link ServerMessage}.
	 */
	void edit(String newContent);

	/**
	 * Gets the author of this {@link ServerMessage message}.
	 *
	 * @return The {@link ServerUser} that authored this {@link ServerMessage}.
	 */
	ServerUser getAuthor();

	/**
	 * Gets the content of this {@link ServerMessage message}.
	 *
	 * @return The content of this {@link ServerMessage}.
	 */
	String getContent();

	/**
	 * Gets the {@link ServerThread thread} that this {@link ServerMessage message} is
	 * contained within.
	 *
	 * @return The {@link ServerThread} that owns this {@link ServerMessage}.
	 */
	ServerThread getThread();
}
