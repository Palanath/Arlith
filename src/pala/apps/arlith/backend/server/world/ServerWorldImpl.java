package pala.apps.arlith.backend.server.world;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.gids.GIDProvider;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.world.ServerCommunity;
import pala.apps.arlith.backend.server.contracts.world.ServerMessage;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.server.contracts.world.ServerWorld;

/**
 * <h1>Server World Implementation</h1>
 * <p>
 * This is the cardinal class of the Server's default implementation of the
 * Server World API (see server world package). This implementation uses the
 * filesystem to store data while offline and is minimal, in that it does not
 * provide much functionality on top of the base specification.
 * </p>
 * <h2>Filesystem</h2>
 * <p>
 * There are many different types of data that need to be stored by the server's
 * world (e.g. data specific to objects, like a user's ID or username, and
 * assets, like a profile image or community background).
 * </p>
 * <p>
 * Storage for the world all goes under the world's {@link #getRootDirectory()
 * root directory}, which can be configured directly when the world
 * {@link #ServerWorldImpl(File, ArlithServer) is constructed}.
 * </p>
 * <p>
 * Data is split into different folders underneath the
 * {@link #getRootDirectory() root directory} based on type. The (current)
 * different tyeps of storage that the world provides are:
 * </p>
 * <style> table, th, td { border: solid 1px currentcolor; border-collapse:
 * collapse; } th, td { padding: 0.3em; } </style>
 * <table>
 * <tr>
 * <th>Storage Type</th>
 * <th style="min-width: 21em;">Path</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Object Storage</td>
 * <td><code>{@link #getRootDirectory() [root-directory]} + '/' + "objects"</code></td>
 * <td>Main form of storage; used to store object data, such as a user's
 * username, or a community's list of members.</td>
 * </tr>
 * <tr>
 * <td>Asset Storage</td>
 * <td><code>{@link #getRootDirectory() [root-directory]} + '/' + "assets"</code></td>
 * <td>Storage for "assets." Assets are external data, usually media (like
 * files, a profile or community icon, or other images), that "belong to" an
 * object, but are a whole, separate piece of media.</td>
 * </tr>
 * </table>
 * <h3>Object Storage</h3>
 * <p>
 * Each object, e.g. {@link ServerCommunity Communities}, {@link ServerUser
 * Users}, {@link ServerMessage Messages}, etc., stores its primary data in its
 * respective folder {@link #getObjectDirectory()}.
 * </p>
 * <p>
 * TODO Include more details on this! TODO Update object storage methods so that
 * they <b>actually align with this documentation</b>.
 * </p>
 * <h3>Asset Storage</h3>
 * <p>
 * Each type of object has its own directory in the
 *
 * @author Palanath
 * @see pala.apps.arlith.backend.server.contracts.world
 *
 */
public class ServerWorldImpl implements ServerWorld {

	private class WorldRegistryImpl extends WorldRegistry {

		public WorldRegistryImpl() {
			super(ServerWorldImpl.this);
		}

		@Override
		public Class<? extends ServerObjectImpl> type(final File file) {
			final File folder = file.getParentFile();
			if (folder.equals(getDirectThreadPath()))
				return ServerDirectThreadImpl.class;
			if (folder.equals(getUserPath()))
				return ServerUserImpl.class;
			else if (folder.equals(getCommunityPath()))
				return ServerCommunityImpl.class;
			else if (folder.equals(getCommunityThreadPath()))
				return ServerCommunityThreadImpl.class;
			else if (folder.equals(getMessagePath()))
				return ServerMessageImpl.class;
			else
				throw new IllegalArgumentException("File not found to be in the folder for any type.");
		}

	}

	public static final String DIRECT_THREAD_STORAGE_LOCATION_SUFFIX = "threads/direct",
			USERS_STORAGE_LOCATION_SUFFIX = "users", COMMUNITIES_STORAGE_LOCATION_SUFFIX = "communities",
			COMMUNITY_THREADS_STORAGE_LOCATION_SUFFIX = "threads/community",
			MESSAGE_STORAGE_LOCATION_SUFFIX = "messages";
	private final File rootDirectory, directThreadPath, userPath, communityPath, communityThreadPath, messagePath;
	private final WorldRegistry registry = new WorldRegistryImpl();
	final GIDProvider gidProvider = new GIDProvider();
	private final ArlithServer server;
	final Map<String, ServerUserImpl> usersByEmail = new HashMap<>(), usersByPhone = new HashMap<>();
	final Map<String, Map<String, ServerUserImpl>> usersByUsername = new HashMap<>();
	final Map<GID, ServerUserImpl> usersByID = new HashMap<>();

	public ServerWorldImpl(final File directory, final ArlithServer server) {
		rootDirectory = directory;

		// Must be added after rootDirectory is set.
		directThreadPath = new File(getObjectDirectory(), DIRECT_THREAD_STORAGE_LOCATION_SUFFIX);
		userPath = new File(getObjectDirectory(), USERS_STORAGE_LOCATION_SUFFIX);
		communityPath = new File(getObjectDirectory(), COMMUNITIES_STORAGE_LOCATION_SUFFIX);
		communityThreadPath = new File(getObjectDirectory(), COMMUNITY_THREADS_STORAGE_LOCATION_SUFFIX);
		messagePath = new File(getObjectDirectory(), MESSAGE_STORAGE_LOCATION_SUFFIX);

		// Set the server.
		this.server = server;

		// Invoke loading of filesystem from directory, if possible.
		try {
			registry.scan();// Should be called after object paths are set.
		} catch (Exception e) {
			// IMPLEMENT: handle exception
			server.getLogger().err(e);
		}
	}

	@Override
	public ServerUser createUserWithEmailAndPhoneUnchecked(final String username, final HexHashValue password,
			final String email, final String phoneNumber) {
		// Check for already used email/phone.
		if (email != null && usersByEmail.containsKey(email))
			return null;
		if (phoneNumber != null && usersByPhone.containsKey(phoneNumber))
			return null;

		final ServerUserImpl user = new ServerUserImpl(this, username, email, phoneNumber, password);

		if (user.hasEmail())
			usersByEmail.put(email, user);
		if (user.hasPhoneNumber())
			usersByPhone.put(phoneNumber, user);
		Map<String, ServerUserImpl> usersByDiscriminator;
		if ((usersByDiscriminator = usersByUsername.get(username)) == null)
			usersByUsername.put(username, usersByDiscriminator = new HashMap<>());
		usersByDiscriminator.put(user.getDiscriminator(), user);
		usersByID.put(user.getGID(), user);

		return user;
	}

	File getCommunityPath() {
		return communityPath;
	}

	File getCommunityThreadPath() {
		return communityThreadPath;
	}

	File getDirectThreadPath() {
		return directThreadPath;
	}

	public GIDProvider getGidProvider() {
		return gidProvider;
	}

	File getMessagePath() {
		return messagePath;
	}

	@Override
	public String getNextDiscriminator(final String username) {
		final Map<String, ServerUserImpl> usersByDisc = usersByUsername.get(username);
		int pivot = (int) (Math.random() * 1000);
		if (usersByDisc == null)
			return pivot < 10 ? "000" + pivot
					: pivot < 100 ? "00" + pivot : pivot < 1000 ? "0" + pivot : String.valueOf(pivot);
		// Loop cyclicly from the randomly selected disc until an available one (in the
		// range 0-9999) is found.
		// TODO Get rid of i variable.
		for (int i = 0; i < 10000; i++, pivot = pivot + 1 % 10000) {
			final String piv = pivot < 10 ? "000" + pivot
					: pivot < 100 ? "00" + pivot : pivot < 1000 ? "0" + pivot : String.valueOf(pivot);
			if (!usersByDisc.containsKey(piv))
				return piv;
		}
		// If all 10K are taken, go to 10000, then 10001, 10002, etc.
		// Note: This does NOT allocate discriminators of length 5+ that have a 0 in any
		// digit but the last four.
		for (pivot = 10000; usersByDisc.containsKey(String.valueOf(pivot)); pivot++)
			;
		return String.valueOf(pivot);
	}

	/**
	 * Returns the {@link File} where objects are stored.
	 *
	 * @return A {@link File} object representing the object data directory.
	 */
	public File getObjectDirectory() {
		return new File(rootDirectory, "objects");
	}

	WorldRegistry getRegistry() {
		return registry;
	}

	public File getRootDirectory() {
		return rootDirectory;
	}

	@Override
	public ArlithServer getServer() {
		return server;
	}

	@Override
	public ServerUser getUserByEmail(final String email) {
		return usersByEmail.get(email);
	}

	@Override
	public ServerUser getUserByPhone(final String phone) {
		return usersByPhone.get(phone);
	}

	@Override
	public ServerUser getUserByUsername(final String username, final String disc) {
		final Map<String, ServerUserImpl> usersByDisc = usersByUsername.get(username);
		return usersByDisc == null ? null : usersByDisc.get(disc);
	}

	File getUserPath() {
		return userPath;
	}

	@Override
	public Collection<? extends ServerCommunity> listPublicCommunities() {
		// TODO Implement this and public communities.
		// For now, this returns an empty list.
		return Collections.emptyList();
	}

	@Override
	public ServerUser getUserByID(GID id) {
		return usersByID.get(id);
	}

}
