package pala.apps.arlith.backend.server.contracts.world;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.types.FriendStateValue;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.server.contracts.media.MediaUpload;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.streams.InputStream;

public interface ServerUser extends ServerObject {

	/**
	 * Represents one user's (the "user") relationship with another user (the
	 * "target") with regard to friendship.
	 *
	 * @author Palanath
	 *
	 */
	enum FriendState {
		/**
		 * <p>
		 * Denotes that a user is friends with another, target user.
		 * </p>
		 * <p>
		 * For any two {@link ServerUser}s, <code>A</code> and <code>B</code>, if
		 * <code>A.getFriendState(B)</code> returns {@link #FRIENDED}, then
		 * <code>B.getFriendState(A)</code> will also return {@link #FRIENDED}.
		 * </p>
		 */
		FRIENDED,
		/**
		 * <p>
		 * Denotes that a user has sent a currently pending friend request to another,
		 * target user.
		 * </p>
		 * <p>
		 * For any two {@link ServerUser}s, <code>A</code> and <code>B</code>, if
		 * <code>A.getFriendState(B)</code> returns {@link #FRIEND_REQUESTED}, then
		 * <code>B.getFriendState(A)</code> will also return {@link #INCOMING_REQUEST}.
		 * </p>
		 */
		FRIEND_REQUESTED,
		/**
		 * <p>
		 * Denotes that a user has a pending friend request from another, target user.
		 * </p>
		 * <p>
		 * For any two {@link ServerUser}s, <code>A</code> and <code>B</code>, if
		 * <code>A.getFriendState(B)</code> returns {@link #INCOMING_REQUEST}, then
		 * <code>B.getFriendState(A)</code> will also return {@link #FRIEND_REQUESTED}.
		 * </p>
		 */
		INCOMING_REQUEST,
		/**
		 * <p>
		 * Denotes that there is no existing friend-relationship between this user and
		 * the other user. The friend-relationship with this value is reflexive.
		 * </p>
		 */
		NONE;

		private static final FriendState[] FRIEND_STATES = values();

		public static FriendState fromOrdinal(final int ord) {
			return FRIEND_STATES[ord];
		}

		public static FriendState fromCommunicationProtocolState(FriendStateValue newState) {
			if (newState == null)
				return null;
			switch (newState) {
			case FRIENDED:
				return FRIENDED;
			case INCOMING:
				return INCOMING_REQUEST;
			case NONE:
				return NONE;
			case OUTGOING:
				return FRIEND_REQUESTED;
			default:
				throw new RuntimeException("Not implemented...");
			}
		}

		public FriendStateValue toCommunicationProtocolState() {
			return toCommunicationProtocol(this);
		}

		public static FriendStateValue toCommunicationProtocol(FriendState fs) {
			if (fs == null)
				return null;
			switch (fs) {
			case FRIEND_REQUESTED:
				return FriendStateValue.OUTGOING;
			case FRIENDED:
				return FriendStateValue.FRIENDED;
			case INCOMING_REQUEST:
				return FriendStateValue.INCOMING;
			default:
				return FriendStateValue.NONE;
			}
		}
	}

	/**
	 * <p>
	 * Changes this user's email address. This method updates this object's
	 * registration in its {@link ServerWorld} accordingly.
	 * </p>
	 * <ul>
	 * <li>If the provided email is the same as the current email address, this
	 * method does nothing.</li>
	 * <li>If the provided email address is <code>null</code>, this user's email
	 * address is unregistered.</li>
	 * </ul>
	 *
	 * @param newEmail The new email address.
	 */
	void changeEmail(String newEmail);

	/**
	 * Changes this user's password. This does not log the user out of any
	 * connections. If the new password is the same as the current password (as
	 * determined through <code>newPassword.equals(currentPassword)</code>), this
	 * method does nothing.
	 *
	 * @param newPassword The new password of the user.
	 */
	void changePassword(HexHashValue newPassword);

	/**
	 * <p>
	 * Changes this user's phone number. This method updates this object's
	 * registration in its {@link ServerWorld} accordingly.
	 * </p>
	 * <ul>
	 * <li>If the provided phone number is the same as the current phone number,
	 * this method does nothing.</li>
	 * <li>If the provided phone number is <code>null</code>, this user's phone
	 * number is unregistered.</li>
	 * </ul>
	 *
	 * @param newPhone The new phone number of this user.
	 */
	void changePhone(String newPhone);

	/**
	 * <p>
	 * Changes this user's username to the specified {@link String} and updates its
	 * discriminator accordingly. This user's registration in the
	 * {@link ServerWorld} that owns it is also accordingly updated.
	 * </p>
	 * <p>
	 * Implementations are free to leave the discriminator unchanged if it is free
	 * for the new username, but are also free to change the discriminator
	 * regardless of whether the previous was valid for the new username.
	 * Immediately after a call to this method, this user should be uniquely
	 * identifiable by its username and discriminator paired. The (possibly new),
	 * valid discriminator is returned.
	 * </p>
	 * <p>
	 * If the provided username is the same as this user's existing username, this
	 * method does nothing and returns the current discriminator.
	 * </p>
	 *
	 * @param newUsername The new username of the user.
	 * @return The discriminator of this user with the new username.
	 */
	String changeUsername(String newUsername);

	/**
	 * Creates a {@link ServerCommunity} with the specified name that is owned by
	 * this {@link ServerUser}.
	 *
	 * @param name The name of the {@link ServerCommunity}.
	 * @return The created {@link ServerCommunity}.
	 */
	ServerCommunity createCommunity(String name);

	/**
	 * <p>
	 * Creates a friend request from this user to the other user, or accepts a
	 * friend request from the other user to this user.
	 * </p>
	 * <ol>
	 * <li>If the {@link FriendState} from this user to the specified user is
	 * {@link FriendState#NONE} or {@link FriendState#FRIEND_REQUESTED}, this method
	 * promotes it to {@link FriendState#FRIEND_REQUESTED}.</li>
	 * <li>Otherwise, this method promotes it to {@link FriendState#FRIENDED} (or
	 * does nothing if it is already {@link FriendState#FRIENDED}.</li>
	 * </ul>
	 * <br>
	 * <style> table, tr, td, th { border: 1px black solid; border-collapse:
	 * collapse; } td { padding: 1em; } th { padding: .5em; } </style>
	 * <table>
	 * <tr>
	 * <th>Current State</th>
	 * <th>=&gt;</th>
	 * <th>New State</th>
	 * <tr>
	 * <td>{@link FriendState#NONE NONE}</td>
	 * <td>=&gt;</td>
	 * <td>{@link FriendState#FRIEND_REQUESTED FRIEND_REQUESTED}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link FriendState#FRIEND_REQUESTED FRIEND_REQUESTED}</td>
	 * <td>=&gt;</td>
	 * <td>{@link FriendState#FRIEND_REQUESTED FRIEND_REQUESTED}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link FriendState#INCOMING_REQUEST INCOMING_REQUEST}</td>
	 * <td>=&gt;</td>
	 * <td>{@link FriendState#FRIENDED FRIENDED}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link FriendState#FRIENDED FRIENDED}</td>
	 * <td>=&gt;</td>
	 * <td>{@link FriendState#FRIENDED FRIENDED}</td>
	 * </tr>
	 * </table>
	 * <br>
	 *
	 * @param other The other user to friend request or accept the request of.
	 */
	void friend(ServerUser other);

	/**
	 * Gets this user's discriminator.
	 *
	 * @return The discriminator of this user.
	 */
	String getDiscriminator();

	/**
	 * Gets this user's email address, or returns <code>null</code> if none is
	 * registered.
	 *
	 * @return This user's registered email address or <code>null</code> if none is
	 *         registered.
	 */
	String getEmail();

	/**
	 * Gets a, possibly unmodifiable, list of {@link ServerUser}s that this
	 * {@link ServerUser} has friend requested.
	 *
	 * @return A possibly unmodifiable list of the users that this user has friend
	 *         requested.
	 */
	List<ServerUser> getFriendRequestedUsers();

	/**
	 * Gets a, possibly unmodifiable, list of {@link ServerUser}s that are friends
	 * with this {@link ServerUser}.
	 *
	 * @return A possibly unmodifiable list of all the {@link ServerUser}s that are
	 *         friended with this {@link ServerUser}.
	 */
	List<ServerUser> getFriends();

	/**
	 * Gets the {@link FriendState} that represents the friendship status between
	 * this and the specified user.
	 *
	 * @param other The other user.
	 * @return The {@link FriendState} between this {@link ServerUser} and the
	 *         {@link ServerUser} specified.
	 */
	FriendState getFriendState(ServerUser other);

	/**
	 * Gets a, possibly unmodifiable, list of {@link ServerUser}s that have friend
	 * requested this user. A call to {@link #getFriendState(ServerUser)} on this
	 * {@link ServerUser} with any of the returned {@link ServerUser}s returns
	 * {@link FriendState#INCOMING_REQUEST}.
	 *
	 * @return A possibly unmodifiable list of the users that have incoming friend
	 *         requests to this user.
	 */
	List<ServerUser> getIncomingFriendRequestUsers();

	/**
	 * Gets the {@link List} of {@link ServerCommunity communities} that this
	 * {@link ServerUser} is in.
	 *
	 * @return The list of {@link ServerCommunity ServerCommunities} that this user
	 *         is in.
	 */
	List<ServerCommunity> getJoinedCommunities();

	/**
	 * <p>
	 * Returns the {@link ServerCommunity} that this {@link ServerUser} is in with
	 * the specified {@link GID}. If this user is not in any {@link ServerCommunity}
	 * with the specified ID, this method returns <code>null</code>. This user must
	 * be <span style="color: hotpink;">a member of</span> the community for this
	 * method to be able to return it.
	 * </p>
	 * <p>
	 * If the provided {@link GID} is <code>null</code>, this method returns
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param id The {@link GID} of the community.
	 * @return The joined {@link ServerCommunity} with the provided {@link GID} or
	 *         <code>null</code> if there is none.
	 */
	default ServerCommunity getJoinedCommunityByID(GID id) {
		for (ServerCommunity c : getJoinedCommunities())
			if (c.getGID().equals(id))
				return c;
		return null;
	}

	/**
	 * Gets this user's phone number, or returns <code>null</code> if none is
	 * registered.
	 *
	 * @return This user's registered phone number or <code>null</code> if none is
	 *         registered.
	 */
	String getPhoneNumber();

	/**
	 * Gets this user's hashed password.
	 * 
	 * @return The user's hashed password.
	 */
	HexHashValue getPassword();

	/**
	 * <p>
	 * Returns the profile icon of this {@link ServerUser} or <code>null</code> if
	 * this user does not have a profile icon.
	 * </p>
	 * <p>
	 * Note that the {@link InputStream} returned <span style="color: red;">may lock
	 * the hard-disk/file-system object that stores the profile icon</span> and may
	 * thusly prevent the {@link #setProfileIcon(byte[]) corresponding setter}
	 * method from operating correctly. (Usually this manifests, on Windows, as the
	 * setter not being able to remove the icon for some time; specifically until
	 * the garbage collector has collected the {@link InputStream}.) The
	 * {@link InputStream} should be closed once its use is completed (as soon as
	 * possible) to make sure that the setter method does not fail unexpectedly.
	 * </p>
	 *
	 * @return An {@link InputStream} object representing this user's profile icon
	 *         or <code>null</code> if this user doesn't have an icon.
	 * @throws FileNotFoundException If a {@link FileNotFoundException} occurs while
	 *                               opening the {@link InputStream}.
	 */
	InputStream getProfileIcon() throws FileNotFoundException;

	/**
	 * Gets this {@link ServerUser}'s username.
	 *
	 * @return The username of this {@link ServerUser}.
	 */
	String getUsername();

	/**
	 * <p>
	 * Returns whether this user has an existing direct thread with the specified
	 * {@link ServerUser}. {@link #openDirectThread(ServerUser)} allows acquiring
	 * the thread.
	 * </p>
	 * <p>
	 * <span style="color: red;">This method must check if this user owns a direct
	 * thread between itself and the specified user OR if the specified user owns a
	 * direct thread between itself and this user.</span> In either case, this
	 * method should return <code>true</code>.
	 * </p>
	 *
	 * @param other The other user to check for a DM with.
	 * @return <code>true</code> if there is an existing direct-message thread with
	 *         the specified user, <code>false</code> otherwise.
	 */
	boolean hasDirectThread(ServerUser other);

	/**
	 * <p>
	 * Returns <code>true</code> if this user has an email address registered.
	 * </p>
	 * <p>
	 * The email address can be obtained with {@link #getEmail()}.
	 * </p>
	 *
	 * @return <code>true</code> if this user has a registered email address,
	 *         <code>false</code> otherwise.
	 */
	default boolean hasEmail() {
		return getEmail() != null;
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this user has a phone number registered.
	 * </p>
	 * <p>
	 * The phone number can be obtained with {@link #getPhoneNumber()}.
	 * </p>
	 *
	 * @return <code>true</code> if this user has a registered phone number,
	 *         <code>false</code> otherwise.
	 */
	default boolean hasPhoneNumber() {
		return getPhoneNumber() != null;
	}

	/**
	 * <p>
	 * Creates or retrieves the direct thread between this user and the specified
	 * user and returns it. To check if a direct-message thread already exists
	 * between this and the specified user, use
	 * {@link #hasDirectThread(ServerUser)}. This method should return the direct
	 * thread between this and the specified user if it exists, <b>whether it is
	 * owned by this user or the specified user</b>.
	 * </p>
	 * <p>
	 * If a thread is created, this {@link ServerUser} becomes the
	 * {@link ServerDirectThread#getStarter() starter} and the user provided as an
	 * argument becomes the {@link ServerDirectThread#getReceiver() receiver}.
	 * </p>
	 * <p style="color: red;">
	 * This method does NOT check for (or behave differently based on) the friend
	 * state between this and the specified user.
	 * </p>
	 *
	 * @param other The user to open the direct-message thread with.
	 * @return The (possibly new) direct thread.
	 */
	ServerDirectThread openDirectThread(ServerUser other);

	/**
	 * Sets this {@link ServerUser}'s profile icon to be the specified
	 * {@link MediaUpload}. If the provided icon argument is <code>null</code>, the
	 * profile icon is removed. Implementations should throw an error if removal
	 * fails, so that the corresponding {@link #getProfileIcon() getter} will not
	 * silently return the purportedly returned image when subsequently called.
	 *
	 * @param icon The new icon for this {@link ServerUser}, or <code>null</code> to
	 *             remove the icon.
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
	void setProfileIcon(byte[] icon)
			throws FileNotFoundException, IOException, UnknownCommStateException, BlockException;

	/**
	 * <p>
	 * Terminates any friend-relationship between this user and the other user.
	 * </p>
	 * <ul>
	 * <li>If there is no friendship status between the users (both users have
	 * {@link FriendState#NONE}), this method does nothing.</li>
	 * <li>If there is an incoming request from the specified user, it is
	 * rejected.</li>
	 * <li>If there is an outgoing friend request to the specified user, it is
	 * rescinded.</li>
	 * <li>If this user and the other are friended, the friendship is
	 * terminated.</li>
	 * </ul>
	 * <p>
	 * After a call to this method, the friend state between this and the specified
	 * {@link ServerUser} will be {@link FriendState#NONE}.
	 * </p>
	 *
	 * @param other The user to terminate the friend state with.
	 */
	void unfriend(ServerUser other);

	/**
	 * Returns this user's tag; that is, this user's {@link #getUsername()
	 * username}, followed by a hashtag (<code>#</code>), followed by their
	 * {@link #getDiscriminator()}.
	 * 
	 * @return <code>{@link #getUsername()} + '#' + {@link #getDiscriminator()}</code>
	 */
	default String getTag() {
		return getUsername() + '#' + getDiscriminator();
	}

	/**
	 * <p>
	 * Searches this user's accessible threads for a {@link ServerThread} with the
	 * specified ID, and returns it if found. If none can be found, this method
	 * returns <code>null</code>.
	 * </p>
	 * <p>
	 * <i>Accessible threads</i> refers to community threads and direct threads that
	 * this user can access. In other words, all threads in all communities that
	 * this user is a member of, and all direct threads between this user and its
	 * current friends. The notion of accessible threads may change in the future,
	 * e.g. when a permissions system is added to communities, when new types of
	 * threads become available, or when direct threads are tracked by user objects.
	 * </p>
	 * <p>
	 * This method searches through the threads of every community this user is a
	 * part of, as well as all direct message threads that this user is a part of.
	 * The standard implementation loops to accomplish this. If any of such threads
	 * has the specified {@link GID}, this method will return it.
	 * </p>
	 * <p>
	 * If the specified {@link GID} is <code>null</code>, this method returns
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param gid The {@link GID} of the thread.
	 * @return The {@link ServerThread} object that this user can access, that has
	 *         the specified {@link GID}, or <code>null</code> if none is found.
	 */
	default ServerThread getAccessibleThreadByID(GID gid) {
		for (ServerCommunity c : getJoinedCommunities())
			for (ServerThread t : c.getThreads())
				if (t.getGID().equals(gid))
					return t;
		for (ServerUser u : getFriends())
			if (hasDirectThread(u)) {
				ServerDirectThread t = openDirectThread(u);
				if (t.getGID().equals(gid))
					return t;
			}
		// No thread was found.
		return null;
	}
}
