package pala.apps.arlith.frontend.server.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.backend.streams.InputStream;
import pala.apps.arlith.frontend.server.contracts.coldstorage.assetowner.AssetOwner;
import pala.apps.arlith.frontend.server.contracts.media.MediaUpload;
import pala.apps.arlith.frontend.server.contracts.world.ServerCommunity;
import pala.apps.arlith.frontend.server.contracts.world.ServerDirectThread;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

final class ServerUserImpl extends ServerObjectImpl implements ServerUser, AssetOwner {

	private final static String USERNAME_KEY = "username", DISCRIMINATOR_KEY = "discriminator", EMAIL_KEY = "email",
			PHONE_KEY = "phone", PASSWORD_KEY = "password", FRIENDSHIPS_KEY = "friendships",
			COMMUNITIES_KEY = "communities", DIRECT_THREADS_KEY = "direct-threads";

	private String username, discriminator, email, phone;
	private HexHashValue password;
	/**
	 * <p>
	 * Keeps track of relationships with other users. This {@link Map} stores other
	 * users (as keys) coupled with the friendship statuses of this user in relation
	 * to that other user (as values).
	 * </p>
	 * <p>
	 * This {@link Map} never stores
	 * {@link pala.apps.arlith.frontend.server.contracts.world.ServerUser.FriendState#NONE}
	 * as a value; the lack of any relationship between users is represented by the
	 * lack of an entry in this {@link Map} for both user objects.
	 * </p>
	 */
	private final Map<ServerUser, FriendState> friendships;

	/**
	 * Stores the communities that this {@link ServerUser} is in. This object is a
	 * {@link List} so that order may be controlled in the future. It should be
	 * maintained that there are never duplicates in this {@link List}.
	 */
	final List<ServerCommunity> communities;

	/**
	 * Maps direct message threads between this user and various other users. The
	 * other user participating in the DM thread is the key of this {@link Map} and
	 * the thread itself is the value.
	 */
	final Map<ServerUser, ServerDirectThread> directThreads;

	public ServerUserImpl(final ServerWorldImpl world, final JSONObject snap) {
		super(snap, world);

		// Simple properties.
		username = getString(snap, USERNAME_KEY);
		discriminator = getString(snap, DISCRIMINATOR_KEY);

		// The following are loaded first so that they are available when other objects
		// (that are loaded from this object) are loaded.
		final JSONArray cm = (JSONArray) snap.get(COMMUNITIES_KEY);
		communities = new ArrayList<>(cm.size());
		final JSONObject dts = (JSONObject) snap.get(DIRECT_THREADS_KEY);
		directThreads = new HashMap<>(dts.size());
		friendships = new HashMap<>();

		// Add user to world (username & discriminator).
		Map<String, ServerUserImpl> usersByDisc = world.usersByUsername.get(username);
		if (usersByDisc == null)
			world.usersByUsername.put(username, usersByDisc = new HashMap<>());
		usersByDisc.put(discriminator, this);
		world.usersByID.put(getGID(), this);

		if (snap.containsKey(EMAIL_KEY))
			world.usersByEmail.put(email = getString(snap, EMAIL_KEY), this);
		if (snap.containsKey(PHONE_KEY))
			world.usersByPhone.put(phone = getString(snap, PHONE_KEY), this);
		password = HexHashValue.createAlreadyHashed(getString(snap, PASSWORD_KEY));

		final JSONObject fs = getObject(snap, FRIENDSHIPS_KEY);
		try {
			for (final Entry<String, JSONValue> e : fs.entrySet())
				friendships.put(world.getRegistry().getUser(GID.fromHex(e.getKey())),
						FriendState.fromOrdinal(fs.getInt(e.getKey())));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("A friend's ID could not be parsed.");
		}

		for (final JSONValue v : cm)
			if (!(v instanceof JSONString))
				throw new IllegalArgumentException("Invalid entry in friend list of user's snapshot.");
			else
				communities.add(world.getRegistry().getCommunity(GID.fromHex(((JSONString) v).getValue())));

		for (final String k : dts.keySet()) {
			final ServerUserImpl u = world.getRegistry().getUser(GID.fromHex(k));
			final ServerDirectThreadImpl t = world.getRegistry().getDirectThread(GID.fromHex(dts.getString(k)));
			directThreads.put(u, t);
		}
	}

	/**
	 * <p>
	 * Creates a new {@link ServerUserImpl} object with the provided username,
	 * email, phone, and password. This constructor <b>does NOT</b> register this
	 * user with the surrounding {@link ServerWorldImpl}.
	 * </p>
	 * <ul>
	 * <li>The <span style="color: green;">discriminator</span> is generated by the
	 * owning {@link ServerWorldImpl} (through a call to
	 * {@link ServerWorldImpl#getNextDiscriminator(String)}) and set automatically,
	 * right after the username is set.</li>
	 * <li>A <span style="color: green;">GID</span> ({@link GID}) is also generated
	 * for this user using the owning {@link ServerWorldImpl}'s
	 * {@link ServerWorldImpl#gidProvider GID provider}.</li>
	 * </ul>
	 * <p>
	 * This constructor makes no checks as to the validity of parameters, although
	 * the following should hold:
	 * </p>
	 * <ol>
	 * <li>The <code>username</code> should be a valid username.</li>
	 * <li>The email may be <code>null</code>.</li>
	 * <li>The <code>phone</code> number may be <code>null</code>.</li>
	 * <li>The <code>password</code> should be a valid password hash.</li>
	 * </ol>
	 * <p>
	 * This constructor <b>saves</b> the newly created {@link ServerUserImpl} to the
	 * file.
	 *
	 * @param username The username of this {@link ServerUser}
	 * @param email    The user's email, or <code>null</code> if this user did not
	 *                 register with one during account creation.
	 * @param phone    The user's phone number, or <code>null</code> if this user
	 *                 did not register with one during account creation.
	 * @param password Hash of the user's password.
	 * @param world    The world that this {@link ServerUserImpl} belongs to.
	 */
	public ServerUserImpl(final ServerWorldImpl world, final String username, final String email, final String phone,
			final HexHashValue password) {
		super(world);
		discriminator = getWorld().getNextDiscriminator(this.username = username);
		this.email = email;
		this.phone = phone;
		this.password = password;
		communities = new ArrayList<>();
		directThreads = new HashMap<>();
		friendships = new HashMap<>();
		save();
	}

	@Override
	public void changeEmail(final String newEmail) {
		// If the emails are the same, do nothing.
		if (!Objects.equals(email, newEmail)) {
			// If the previous email was not null, we need to unregister the email->user
			// mapping in the world.
			if (email != null)
				getWorld().usersByEmail.remove(email);
			// If the new email is not null, we need to register a new email->user mapping
			// in the world.
			if (newEmail != null)
				getWorld().usersByEmail.put(email = newEmail, this);
			save();
		}
	}

	@Override
	public void changePassword(final HexHashValue newPassword) {
		if (!newPassword.equals(password)) {
			password = newPassword;
			save();
		}
	}

	/**
	 * <p>
	 * Changes this user's phone number to the specified phone number. If the
	 * specified phone number is <code>null</code>, this user's phone number is
	 * cleared.
	 * </p>
	 * <p>
	 * This method does nothing if the current phone number and the new phone number
	 * are the same (whether they are a certain value, or <code>null</code>).
	 * </p>
	 * <p>
	 * This method saves this {@link ServerUserImpl} if a change took place.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changePhone(final String newPhone) {
		if (!Objects.equals(phone, newPhone)) {
			if (phone != null)
				getWorld().usersByPhone.remove(phone);
			if (newPhone != null)
				getWorld().usersByPhone.put(phone = newPhone, this);
			save();
		}
	}

	@Override
	public String changeUsername(final String newUsername) {
		if (newUsername.equals(username))
			return discriminator;
		final Map<String, ServerUserImpl> usersByDisc = getWorld().usersByUsername.get(username);
		usersByDisc.remove(discriminator);// Remove this user from the username pool.

		// If there are no longer any users with that username, remove the entire
		// discriminator->user map from the username->(discriminator->user) map.
		// This is solely for performance/prevention of a memory leak
		if (usersByDisc.isEmpty())
			getWorld().usersByUsername.remove(username);
		// Update the username and the discriminator, and return the new discriminator.
		String newDisc = discriminator = getWorld().getNextDiscriminator(username = newUsername);
		Map<String, ServerUserImpl> usersWithSameName = getWorld().usersByUsername.get(username);
		if (usersWithSameName == null)
			getWorld().usersByUsername.put(username, usersWithSameName = new HashMap<>());
		usersWithSameName.put(newDisc, this);

		save();
		return newDisc;
	}

	private List<ServerUser> collectRelated(final FriendState state) {
		final List<ServerUser> users = new ArrayList<>();
		for (final Entry<ServerUser, FriendState> fe : friendships.entrySet())
			if (fe.getValue() == state)
				users.add(fe.getKey());
		return users;
	}

	@Override
	public ServerCommunity createCommunity(final String name) {
		ServerCommunityImpl comm = new ServerCommunityImpl(getWorld(), this, name);
		communities.add(comm);
		save();
		return comm;
	}

	@Override
	public void friend(final ServerUser other) {
		if (getFriendState(other) == FriendState.INCOMING_REQUEST) {// If this user's relationship with the other
																	// user is "incoming request"
			// Then we want to accept that friend request.
			friendships.put(other, FriendState.FRIENDED);
			((ServerUserImpl) other).friendships.put(this, FriendState.FRIENDED);
			save();
			((ServerUserImpl) other).save();
		} else if (getFriendState(other) == FriendState.NONE) {// There is no present state. Send a request.
			// The condition of this if could also be
			// friendships.containsKey(other.getGID());

			// Send the request.
			friendships.put(other, FriendState.FRIEND_REQUESTED);
			((ServerUserImpl) other).friendships.put(this, FriendState.INCOMING_REQUEST);
			save();
			((ServerUserImpl) other).save();
		}
	}

	/**
	 * <p>
	 * Returns this user's Asset Directory. The asset directory is a central
	 * location for user's to store assets.
	 * </p>
	 * <p>
	 * The assets directory is <code>{@link ServerWorldImpl#getRootDirectory()
	 * [world-directory]}/assets/{@link ServerWorldImpl#USERS_STORAGE_LOCATION_SUFFIX}/[user-GID]/</code>
	 * where:
	 * </p>
	 * <ul>
	 * <li><code>[world-directory]</code> is
	 * {@link ServerWorldImpl#getRootDirectory() the data directory} for the
	 * surrounding {@link ServerWorldImpl}, and</li>
	 * <li><code>[user-GID]</code> is {@link #getGID() the user's GID} in hex
	 * form.</li>
	 * </ul>
	 *
	 * @return This user's Asset directory, as a {@link File}. Assets, like a user's
	 *         profile icon, can be stored here.
	 */
	public @Override File getAssetDirectory() {
		// TODO Change the path for this.
		return new File(getWorld().getRootDirectory(),
				"assets/" + ServerWorldImpl.USERS_STORAGE_LOCATION_SUFFIX + '/' + getGID().getHex());
	}

	@Override
	public String getDiscriminator() {
		return discriminator;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public List<ServerUser> getFriendRequestedUsers() {
		return collectRelated(FriendState.FRIEND_REQUESTED);
	}

	@Override
	public List<ServerUser> getFriends() {
		return collectRelated(FriendState.FRIENDED);
	}

	@Override
	public FriendState getFriendState(final ServerUser other) {
		return friendships.containsKey(other) ? friendships.get(other) : FriendState.NONE;
	}

	@Override
	public List<ServerUser> getIncomingFriendRequestUsers() {
		return collectRelated(FriendState.INCOMING_REQUEST);
	}

	@Override
	public List<ServerCommunity> getJoinedCommunities() {
		return communities;
	}

	@Override
	public String getPhoneNumber() {
		return phone;
	}

	/**
	 * <p>
	 * Returns an {@link InputStream} that reads the profile icon from this user's
	 * <i>assets directory</i>. For more information on users' assets directories,
	 * see {@link #getAssetDirectory()}.
	 * </p>
	 * <p>
	 * The profile icon is stored at <code>[asset-dir]/profile-icon.img</code> where
	 * <code>[asset-dir]</code> is {@link #getAssetDirectory() this user's assets
	 * directory}.
	 * </p>
	 * <p>
	 * The returned {@link InputStream}'s reads are synchronized against other
	 * writes.
	 * </p>
	 * 
	 * @throws FileNotFoundException In case an error occurs while opening the
	 *                               {@link FileInputStream} (which results in a
	 *                               {@link FileNotFoundException}). See the
	 *                               {@link FileInputStream} class and the
	 *                               {@link FileInputStream#FileInputStream(File)}
	 *                               constructor for more details.
	 */
	@Override
	public InputStream getProfileIcon() throws FileNotFoundException {
		return getProfileIconFile().isFile()
				? InputStream.fromJavaInputStream(new FileInputStream(getProfileIconFile()))
				: null;
	}

	private File getProfileIconFile() {
		return new File(getAssetDirectory(), "/profile-icon.img");
	}

	@Override
	public File getStorageFile() {
		return new File(getWorld().getUserPath(), getGID().getHex() + ".aso");
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean hasDirectThread(final ServerUser other) {
		return other != null
				&& (directThreads.containsKey(other) || ((ServerUserImpl) other).directThreads.containsKey(this));
	}

	@Override
	public ServerDirectThread openDirectThread(final ServerUser other) {
		ServerDirectThread thr;
		// See documentation: This method must check for (1) the case that this user
		// created a direct thread between itself and the specified user (already) and
		// (2) the case that the specified user opened a direct thread between itself
		// and this user. In either case, the existing direct thread should be returned.
		if ((thr = directThreads.get(other)) == null
				&& (thr = ((ServerUserImpl) other).directThreads.get(this)) == null)
			directThreads.put(other, thr = new ServerDirectThreadImpl(getWorld(), this, other));
		// TODO Add to world if world handles direct threads.
		return thr;
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		// First invoke super call.
		super.restore(snap);

		// Then check to make sure the new username and discriminator pair are valid:
		String un = snap.getString(USERNAME_KEY), disc = snap.getString(DISCRIMINATOR_KEY);
		if ((!un.equals(username) || !disc.equals(discriminator))) {
			// Check if the new pair is taken.
			if (getWorld().checkIfUserExists(un, disc))
				throw new IllegalArgumentException(
						"User with username and discriminator to be restored already exists.");
		} else
			un = disc = null;// Signify that these do not need to be updated.
		// Once we've arrived at this point, it is either the case that [un and disc are
		// null, meaning they do not need to be updated] or [a change of username needs
		// to be made and can be made]. The checks below will generally follow this
		// layout.

		String e = snap.getString(EMAIL_KEY);
		if (!e.equals(email)) {
			if (getWorld().checkIfEmailTaken(e))
				throw new IllegalArgumentException("The email to be restored is already in use.");
		} else
			e = null;

		String p = snap.getString(PHONE_KEY);
		if (!p.equals(phone)) {
			if (getWorld().checkIfPhoneTaken(p))
				throw new IllegalArgumentException("The phone to be restored is already in use.");
		} else
			p = null;

		password = HexHashValue.createAlreadyHashed(snap.getString(PASSWORD_KEY));

		// Make sure all friendships are valid.
//		JSONObject o = (JSONObject) snap.get(FRIENDSHIPS_KEY);
//		for (Entry<String, JSONValue> entry : o.entrySet()) {
//			// Each friendship can be checked for validity individually.
//		}
		// TODO IMPLEMENT This needs to be implemented? and verified for when events are
		// reimplemented.

	}

	@Override
	public JSONObject snapshot() {
		final JSONObject snapshot = super.snapshot();

		// Simple Properties
		snapshot.put(USERNAME_KEY, username).put(DISCRIMINATOR_KEY, discriminator).put(PASSWORD_KEY,
				password.getHash());
		if (hasEmail())
			snapshot.put(EMAIL_KEY, email);
		if (hasPhoneNumber())
			snapshot.put(PHONE_KEY, phone);

		// Friendships
		final JSONObject fs = new JSONObject();
		for (final Entry<ServerUser, FriendState> e : friendships.entrySet())
			fs.put(e.getKey().getGID().getHex(), e.getValue().ordinal());
		snapshot.put(FRIENDSHIPS_KEY, fs);

		// Communities
		snapshot.put(COMMUNITIES_KEY,
				new JSONArray(JavaTools.mask(communities, a -> new JSONString(a.getGID().getHex()))));

		// Direct Threads
		final JSONObject dts = new JSONObject();
		for (final Entry<ServerUser, ServerDirectThread> e : directThreads.entrySet())
			dts.put(e.getKey().getGID().getHex(), e.getValue().getGID().getHex());
		snapshot.put(DIRECT_THREADS_KEY, dts);

		return snapshot;
	}

	@Override
	public void unfriend(final ServerUser other) {
		friendships.remove(other);
		ServerUserImpl otherUser = (ServerUserImpl) other;
		otherUser.friendships.remove(this);
		save();
		otherUser.save();
	}

	@Override
	public HexHashValue getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return getUsername() + '#' + getDiscriminator();
	}

	/**
	 * <p>
	 * Writes the data from the provided {@link MediaUpload} into this user's
	 * profile icon file. The profile icon is stored at
	 * <code>[asset-dir]/profile-icon.img</code> where <code>[asset-dir]</code> is
	 * {@link #getAssetDirectory() this user's assets directory}. The asset
	 * directory is created if it does not yet exist. It is <b>not</b> deleted if it
	 * becomes empty through a call to this method.
	 * </p>
	 * <p>
	 * The write is synchronized against all other writes and all
	 * {@link #getProfileIcon() reads from getProfileIcon()'s returned Icon's
	 * reads}.
	 * </p>
	 * 
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
	@Override
	public void setProfileIcon(byte[] icon)
			throws FileNotFoundException, IOException, UnknownCommStateException, BlockException {
		File file = getProfileIconFile();
		if (icon == null)
			Files.deleteIfExists(file.toPath());
		else {
			file.getParentFile().mkdirs();
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(icon);
			}
		}
	}

}