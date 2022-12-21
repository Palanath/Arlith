package pala.apps.arlith.frontend.server.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.frontend.server.utils.ServerUtils;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Maintains a registry of all {@link ServerWorldImpl} objects during
 * {@link ServerWorldImpl} bootup from the filesystem.
 * </p>
 * <p>
 * When the {@link ServerWorldImpl} boots up from a storage location, it loads
 * each world object it finds in, one at a time. Some world objects refer to
 * other world objects, and need to satisfy these references before they finish
 * being constructed into complete Java objects. To permit this to happen, this
 * {@link WorldRegistry} stores references to each world object by ID. Such
 * objects are registered with this registry <i>before their object construction
 * begins</i>, so that objects of the same type, or other objects currently
 * being constructed, may refer to them and store such references. This
 * registration with a {@link ServerWorldImpl#getRegistry() ServerWorldImpl's
 * registry} is performed in the {@link ServerObjectImpl} constructor designed
 * to load an object from cold-storage (see
 * {@link ServerObjectImpl#ServerObjectImpl(GID, ServerWorldImpl)}).
 * </p>
 * <p>
 * When a {@link ServerObjectImpl} begins to be constructed, it is registered in
 * this registry by its {@link ServerObjectImpl#getGID() GID} under every class
 * from its {@link ServerObjectImpl#getClass() instance class type} to
 * {@link ServerObjectImpl}. For example, a {@link ServerDirectThreadImpl
 * ServerDirectThreadImpl} would be registered in this registry under the
 * {@link ServerDirectThreadImpl ServerDirectThreadImpl} class type, the
 * {@link ServerThreadImpl ServerThreadImpl} class type, and the
 * {@link ServerObjectImpl} class type. If it refers to another object, that
 * directly or indirectly refers back to it, this reference can be acquired
 * through this {@link WorldRegistry}, regardless of the type of object expected
 * of the reference.
 * </p>
 *
 * @author Palanath
 *
 */
abstract class WorldRegistry {

	private final ServerWorldImpl world;

	private final Map<Class<? extends ServerObjectImpl>, Map<GID, ServerObjectImpl>> registry = new HashMap<>();

	private final Map<GID, File> objectMapping = new HashMap<>();

	/**
	 * Constructs a new {@link WorldRegistry} that is ready to be invoked through a
	 * call to {@link #scan()}.
	 * 
	 * @param world The world that this registry is meant to load.
	 */
	public WorldRegistry(final ServerWorldImpl world) {
		this.world = world;
	}

	/**
	 * Scans the entire {@link #world}'s {@link ServerWorldImpl#getObjectDirectory()
	 * object directory}, and then loads all the objects that were scanned.
	 */
	public void scan() {
		// Scan the entire object directory.
		scanDir(world.getObjectDirectory());

		// Load the world up.

		for (Entry<GID, File> e : objectMapping.entrySet()) {
			final Class<? extends ServerObjectImpl> c = type(e.getValue());
			if (c == ServerDirectThreadImpl.class)
				getDirectThread(e.getKey());
			else if (c == ServerUserImpl.class)
				getUser(e.getKey());
			else if (c == ServerCommunityImpl.class)
				getCommunity(e.getKey());
			else if (c == ServerCommunityThreadImpl.class)
				getCommunityThread(e.getKey());
			else if (c == ServerMessageImpl.class)
				getMessage(e.getKey());
			else if (ServerThreadImpl.class.isAssignableFrom(c))
				getThread(e.getKey());
			else
				System.out.println(
						"Unknown object not directly loaded: " + e.getKey() + ". Type noted as: " + c.getSimpleName());
		}
	}

	public File apply(final GID t) {
		return objectMapping.get(t);
	}

	/**
	 * Returns the community with the provided ID. If the community has not yet been
	 * loaded, it is loaded from the filesystem.
	 *
	 * @param communityID The {@link GID} of the community.
	 * @return The community.
	 */
	public ServerCommunityImpl getCommunity(final GID communityID) {
		final ServerCommunityImpl i = lookup(ServerCommunityImpl.class, communityID);
		return i == null ? new ServerCommunityImpl(world, loadSnapshot(apply(communityID))) : i;
	}

	/**
	 * Gets a loaded community thread by ID. The community thread
	 * <b style="color: red;">must have already been loaded</b> via loading of the
	 * community that it belongs to. If such is not the case, this method will throw
	 * an {@link IllegalStateException}.
	 *
	 * @param threadID The {@link GID} of the community thread.
	 * @return The community thread.
	 * @throws IllegalStateException If the community thread desired is not loaded
	 *                               yet.
	 */
	public ServerCommunityThreadImpl getCommunityThread(final GID threadID) throws IllegalStateException {
		final ServerCommunityThreadImpl i = lookup(ServerCommunityThreadImpl.class, threadID);
		return i == null ? new ServerCommunityThreadImpl(world, loadSnapshot(apply(threadID))) : i;
	}

	/**
	 * Returns the direct thread with the provided ID. If the direct thread has not
	 * yet been loaded, it is loaded from the filesystem.
	 *
	 * @param threadID The {@link GID} of the thread.
	 * @return The direct thread.
	 */
	public ServerDirectThreadImpl getDirectThread(final GID threadID) {
		final ServerDirectThreadImpl i = lookup(ServerDirectThreadImpl.class, threadID);
		return i == null ? new ServerDirectThreadImpl(world, loadSnapshot(apply(threadID))) : i;
	}

	/**
	 * Gets a loaded message by ID. The message <b style="color: red;">must have
	 * already been loaded</b> via loading of the thread that it belongs to. If such
	 * is not the case, this method will throw an {@link IllegalStateException}.
	 *
	 * @param messageID The {@link GID} of the message.
	 * @return The message.
	 * @throws IllegalStateException If the message desired is not loaded yet.
	 */
	public ServerMessageImpl getMessage(final GID messageID) throws IllegalStateException {
		final ServerMessageImpl i = lookup(ServerMessageImpl.class, messageID);
		return i == null ? new ServerMessageImpl(world, loadSnapshot(apply(messageID))) : i;
	}

	public ServerThreadImpl getThread(final GID gid) {
		final ServerThreadImpl i = lookup(ServerThreadImpl.class, gid);
		if (i != null)
			return i;
		final File file = apply(gid);
		final Class<? extends ServerThreadImpl> c = type(file);
		if (c == ServerDirectThreadImpl.class)
			// Load a direct thread.
			return new ServerDirectThreadImpl(world, loadSnapshot(file));
		else if (c == ServerCommunityThreadImpl.class)
			// Load comm thread.
			return new ServerCommunityThreadImpl(world, loadSnapshot(file));
		else
			throw new RuntimeException("Unknown class type returned from Filemapping#type: " + c.getCanonicalName());
	}

	/**
	 * Returns the user with the provided ID. If the user has not yet been loaded,
	 * it is loaded from the filesystem.
	 *
	 * @param userID The {@link GID} of the user.
	 * @return The user.
	 */
	public ServerUserImpl getUser(final GID userID) {
		final ServerUserImpl i = lookup(ServerUserImpl.class, userID);
		return i == null ? new ServerUserImpl(world, loadSnapshot(apply(userID))) : i;
	}

	private JSONObject loadSnapshot(final File file) {
		try (FileInputStream fis = new FileInputStream(file)) {
			return ServerUtils.read(fis);
		} catch (final FileNotFoundException e) {
			throw new IllegalArgumentException("Object with specified type and GID not found or could not be loaded.");
		} catch (IOException e1) {
			System.err.println("Error while loading file: " + file);
			e1.printStackTrace();
			throw new RuntimeException("File loading error.");
		}
	}

	/**
	 * Looks up the object of the specified type with the specified {@link GID} in
	 * this {@link WorldRegistry}. If the object is not registered, this method
	 * returns <code>null</code>.
	 *
	 * @param <O>  The type of the object.
	 * @param type The class of the type of the object.
	 * @param gid  The {@link GID} of the object.
	 * @return The object, or <code>null</code> if it's not registered.
	 */
	private <O extends ServerObjectImpl> O lookup(final Class<O> type, final GID gid) {
		Map<GID, ServerObjectImpl> map = registry.get(type);
		return map == null ? null : type.cast(map.get(gid));
	}

	/**
	 * Registers this {@link ServerObjectImpl} into this {@link WorldRegistry} so
	 * that other objects may reference it during world bootup from cold storage.
	 *
	 * @param object The object to register.
	 */
	@SuppressWarnings("unchecked")
	void register(final ServerObjectImpl object) {

		// TODO Registering every object under ServerObjectImpl doubles the number of
		// references. If possible, maybe it should be removed.

		Class<?> c = object.getClass();
		// Class is guaranteed to extend ServerObjectImpl so we check such condition
		// after one insertion.
		do {
			Map<GID, ServerObjectImpl> map = registry.get(c);
			if (map == null)
				registry.put((Class<? extends ServerObjectImpl>) c, map = new HashMap<>());
			map.put(object.getGID(), object);
		} while (ServerObjectImpl.class.isAssignableFrom(c = c.getSuperclass()));
	}

	private void scanDir(final File dir) {
		// TODO Handle NullPointerException.
		final File[] files = dir.listFiles();
		if (files == null)
			return;// Do nothing if no children files to scan have been found.
		for (File f : files)
			if (f.isFile()) {
				if (f.getName().endsWith(".aso")) {
					final String name = f.getName();
					// TODO Handle fromHex's NumberFormatException.
					if ((f = objectMapping.put(GID.fromHex(name.substring(0, name.indexOf('.'))), f)) != null)
						System.err.println("Two ServerObjects with same ID exist in filesystem. Second file: " + f);
				}
			} else /* if (f.isDirectory()) */
				scanDir(f);
	}

	abstract <C extends ServerObjectImpl> Class<? extends C> type(File file);

}
