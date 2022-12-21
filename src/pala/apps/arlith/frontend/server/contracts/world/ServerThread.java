package pala.apps.arlith.frontend.server.contracts.world;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import pala.apps.arlith.backend.communication.gids.GID;

public interface ServerThread extends ServerObject {
	/**
	 * Checks if this {@link ServerThread} contains the specified {@link ServerMessage}.
	 *
	 * @param message The message to check for containment of.
	 * @return <code>true</code> if this {@link ServerThread} contains the specified
	 *         message. <code>false</code> otherwise.
	 */
	boolean containsMessage(ServerMessage message);

	/**
	 * Deletes all of the messages in the specified {@link Collection}. The elements
	 * need not be in any particular order. Duplicate elements are ignored. Messages
	 * not contained inside this {@link ServerThread} are ignored.
	 *
	 * @param messages A {@link Collection} containing each {@link ServerMessage} that
	 *                 will be deleted.
	 */
	void deleteMessages(Collection<? extends ServerMessage> messages);

	/**
	 * <p>
	 * Deletes the specified number of messages immediately after the provided pivot
	 * message. If there are not as many messages after the pivot message as is
	 * specified, this method deletes as many as there are. If the proivded pivot
	 * message is not in this {@link ServerThread}, this method throws an
	 * {@link IllegalArgumentException}.
	 * </p>
	 * <p>
	 * This method deletes messages in chronological order, starting at the message
	 * immediately after the provided pivot message, if any.
	 * </p>
	 *
	 * @param pivot  The pivot message which messages sent after will be deleted.
	 * @param amount The number of messages to delete.
	 * @return The number of messages actually deleted.
	 * @throws IllegalArgumentException If the provided pivot {@link ServerMessage} is
	 *                                  not a member of this {@link ServerThread}.
	 */
	int deleteMessagesAfter(ServerMessage pivot, int amount) throws IllegalArgumentException;

	/**
	 * <p>
	 * Deletes the specified number of messages immediately before the provided
	 * pivot message. If there are not as many messages before the pivot message as
	 * is specified, this method deletes as many as there are. If the provided pivot
	 * message is not in this {@link ServerThread}, this method throws an
	 * {@link IllegalArgumentException}.
	 * </p>
	 * <p>
	 * This method deletes messages in reverse chronological order, starting at the
	 * message immediately before the provided pivot message, if any.
	 * </p>
	 *
	 * @param pivot  The pivot message which messages before will be deleted.
	 * @param amount The number of messages to delete.
	 * @return The number of deleted messages.
	 * @throws IllegalArgumentException If the provided pivot {@link ServerMessage} is
	 *                                  not a member of this {@link ServerThread}.
	 */
	int deleteMessagesBefore(ServerMessage pivot, int amount) throws IllegalArgumentException;

	/**
	 * Deletes all of the messages with the provided {@link GID}s contained in the
	 * specified {@link Collection}. The elements need not be in any particular
	 * order. Duplicate elements are ignored. Elements which do not point to valid
	 * {@link ServerMessage}s or do not point to {@link ServerMessage}s contained in this
	 * {@link ServerThread} are ignored.
	 *
	 * @param messages A {@link Collection} of {@link GID}s of the
	 *                 {@link ServerMessage}s to delete.
	 */
	void deleteMessagesByID(Collection<GID> messages);

	/**
	 * <p>
	 * Returns a modifiable {@link List} of the earliest {@link ServerMessage}s sent in
	 * this {@link ServerThread}. The number of messages is the lesser of the number of
	 * messages in this {@link ServerThread} and the specified count. (This method
	 * returns <code>count</code> messages, or as many messages as exist in this
	 * thread if there are fewer in this thread than <code>count</code>.)
	 * </p>
	 * <p>
	 * The returned {@link List} is ordered chronologically: Messages with a lower
	 * index were created before messages with a higher index.
	 * </p>
	 *
	 * @param count The number of messages to retrieve.
	 * @return A new {@link List} containing all of the messages that were
	 *         retrieved, in order.
	 */
	List<? extends ServerMessage> getEarliestMessages(int count);

	/**
	 * <p>
	 * Returns a modifiable {@link List} of the latest {@link ServerMessage}s sent in
	 * this {@link ServerThread}. The number of messages is the lesser of the number of
	 * messages in this {@link ServerThread} and the specified count. (This method
	 * returns <code>count</code> messages, or as many messages as exist in this
	 * thread if there are fewer in this thread than <code>count</code>.)
	 * </p>
	 * <p>
	 * The returned {@link List} is ordered chronologically: Messages with a lower
	 * index were created before messages with a higher index.
	 * </p>
	 *
	 * @param count The number of messages to retrieve.
	 * @return A new {@link List} containing all of the messages that were
	 *         retrieved, in order.
	 */
	List<? extends ServerMessage> getLatestMessages(int count);

	/**
	 * Gets all the {@link ServerUser}s that have access to this {@link ServerThread} as an
	 * unmodifiable {@link Collection}. They may have different permissions or
	 * different levels of access, but are all considered "members" of the thread.
	 *
	 * @return An unmodifiable {@link Collection} of the {@link ServerUser}s contained
	 *         within this {@link ServerThread}.
	 */
	Collection<? extends ServerUser> getParticipants();

	/**
	 * <p>
	 * Gets the specified number of messages sent immediately before the specified
	 * {@link ServerMessage}. The number of messages is the lesser of <code>count</code>
	 * and the number of messages before the specified <code>pivot</code> message
	 * that exist in this {@link ServerThread}.
	 * </p>
	 * <p>
	 * The returned {@link List} is ordered chronologically: Messages with a lower
	 * index were created before messages with a higher index.
	 * </p>
	 *
	 * @param pivot The pivot message; this function retrieves <code>count</code>
	 *              messages before this message.
	 * @param count The number of messages before the <code>pivot</code> message to
	 *              retrieve.
	 * @return A new {@link List} containing all of the messages that were
	 *         retrieved, in order.
	 * @throws IllegalArgumentException If the specified pivot {@link ServerMessage} is
	 *                                  not contained within this {@link ServerThread}.
	 */
	List<? extends ServerMessage> getPreviousMessages(ServerMessage pivot, int count) throws IllegalArgumentException;

	/**
	 * <p>
	 * Gets the specified number of messages sent immediately after the specified
	 * {@link ServerMessage}. The number of messages is the lesser of <code>count</code>
	 * and the number of messages before the specified <code>pivot</code> message
	 * that exist in this {@link ServerThread}.
	 * </p>
	 * <p>
	 * The returned {@link List} is ordered chronologically: Messages with a lower
	 * index were created before messages with a higher index.
	 * </p>
	 *
	 * @param pivot The pivot message; this function retrieves <code>count</code>
	 *              messages that are after this message.
	 * @param count The number of messages after the <code>pivot</code> message to
	 *              retrieve.
	 * @return A new {@link List} containing all of the messages that were
	 *         retrieved, in order.
	 * @throws IllegalArgumentException If the specified pivot {@link ServerMessage} is
	 *                                  not contained within this {@link ServerThread}.
	 */
	List<? extends ServerMessage> getSubsequentMessages(ServerMessage pivot, int count) throws IllegalArgumentException;

	/**
	 * Returns whether this user is a member of this thread. This is (currently) the
	 * only factor used to determine whether a message can be sent by a user in a
	 * thread or not.
	 *
	 * @param user The user.
	 * @return <code>true</code> if the specified user is in this thread.
	 *         <code>false</code> otherwise.
	 */
	default boolean isParticipant(final ServerUser user) {
		return getParticipants().contains(user);
	}

	/**
	 * <p>
	 * Returns an unmodifiable, ordered {@link List} of the {@link ServerMessage}s
	 * inside this {@link ServerThread}. The returned {@link List} object is a
	 * <i>view</i> of the {@link ServerMessage} list that this {@link ServerThread} uses, so
	 * changes to the {@link ServerMessage} list of this {@link ServerThread} are reflected
	 * by the returned {@link List} (e.g., when a message is sent in this thread, it
	 * gets appended to the returned list).
	 * </p>
	 * 
	 * @return A new, unmodifiable view of the {@link ServerMessage} list backing this
	 *         {@link ServerThread}.
	 */
	List<? extends ServerMessage> getMessages();

	default ServerMessage getMessageByID(GID id) {
		// Do a binary search to get the position of the object. Since we only have a
		// GID, we have to use a faux object to search the list using
		// Collections.binarySearch(...) (or, we could have instead used a custom
		// comparator).
		int ind = Collections.binarySearch(getMessages(), ServerObject.fauxComparableObject(id));
		// If binarySearch checks equality using compareTo(...), it should return a
		// positive number only if a match was found.
		// If binarySearch checks equality using equals(), it should return a negative
		// number, always.
		//
		// We don't know which, of the two ways above, that Collections.binarySearch was
		// implemented, so we handle them both.

		// If we found a match, perfect. Return it directly.
		if (ind >= 0)
			return getMessages().get(ind);
		else {
			// If we didn't find a match, it means that Collections.binarySearch(...)
			// returned:
			// (-(hypotheticalPosition) - 1)
			// where hypotheticalPosition is the position in the list that the message we're
			// searching for WOULD be, assuming it were in the list and it has the ID we
			// want. Problematically, it might still be in the list, but since it is not
			// "equal" to our faux object, Collections.binarySearch(...) may have thought it
			// was different, and returned a negative index.
			//
			// We need to check that location in the list to see if any of the messages
			// around the position share the same GID. Since each object has a unique GID
			// (except for the faux object we use for the search), we only need to check the
			// two messages "surrounding" the index returned.

			int purportedIndex = -ind - 1;// Number is now guaranteed to be positive (or zero).

			// What we want to do here is check the message AT the returned, "hypothetical"
			// position, and BEFORE the hypothetical position. The reason we check "at" and
			// "before" instead of "at" and "after" is because, whenever a message is added
			// to position x, everything AT AND INCLUDING the previous message at x gets
			// MOVED UP. Therefore, the message we're looking for is either, currently at x,
			// or right before x.
			//
			// Collections.binarySearch had to return a hypothetical position that would
			// either go before (==> index is at message, check message AT index) the valid
			// message or after (==> index is after, meaning we check the message BEFORE the
			// index) the valid message.

			// Also, the reason we perform checks in both cases is because it could very
			// well be the case that the binary search function returned the hypothetical
			// position, BUT that the GIDs did not match any messages at all (rather than
			// the GIDs matching, but the function still returning a hypothetical index,
			// because the faux object was "not equal" to the real, same-ID message)! This
			// would mean that the message is not contained.
			if (purportedIndex != getMessages().size()) {
				ServerMessage m = getMessages().get(purportedIndex);
				if (m.getGID().equals(id))
					return m;
			}
			if (purportedIndex-- != 0) {
				ServerMessage m = getMessages().get(purportedIndex);
				if (m.getGID().equals(id))
					return m;
			}
			// Nothing found. Return null.
			return null;
		}
	}

	/**
	 * <p>
	 * Sends a {@link String} message from the specified author with the specified
	 * text in this {@link ServerThread}. If the {@link ServerUser} is not a member of this
	 * {@link ServerThread}, this method throws an {@link IllegalArgumentException}.
	 * </p>
	 * <p>
	 * The created message is made as soon as possible and is added to this thread
	 * as soon as possible. {@link ServerMessage}s are {@link Comparable ordered} by
	 * their {@link GID}s, which store the timestamp that the message was created at
	 * (and uses it in the ordering). This means that {@link ServerMessage}s have a
	 * {@link ServerMessage#compareTo(ServerObject)} method, which allows a developer to
	 * check whether a {@link ServerMessage} was made <i>before</i> or <i>after</i>
	 * another {@link ServerMessage}, chronologically. This method is guaranteed to
	 * insert the message that it creates (which it returns) into the list in a
	 * consistent, sorted fashion. (The list will always remain sorted.)
	 * </p>
	 *
	 * @param text   The {@link String} content of the message to send.
	 * @param author The author of the message.
	 * @return A new {@link ServerMessage} object representing the message that was
	 *         sent.
	 * @throws IllegalArgumentException If the provided {@link ServerUser} is not a
	 *                                  participant of this thread.
	 */
	ServerMessage sendMessage(String text, ServerUser author) throws IllegalArgumentException;
}
