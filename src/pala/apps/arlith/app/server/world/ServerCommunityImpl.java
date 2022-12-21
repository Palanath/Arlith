package pala.apps.arlith.app.server.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pala.apps.arlith.app.server.contracts.coldstorage.assetowner.AssetOwner;
import pala.apps.arlith.app.server.contracts.world.ServerCommunity;
import pala.apps.arlith.app.server.contracts.world.ServerCommunityThread;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.backend.streams.InputStream;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

class ServerCommunityImpl extends ServerObjectImpl implements ServerCommunity, AssetOwner {

	private static final String NAME_KEY = "name", PARTICIPANTS_KEY = "participants", THREADS_KEY = "threads_key",
			OWNER_KEY = "owner";

	private String name;
	final Set<ServerUserImpl> participants;
	final List<ServerCommunityThreadImpl> threads = new ArrayList<>();
	private ServerUserImpl owner;

	/**
	 * <p>
	 * Creates a community belonging to the specified world and owned by the
	 * specified user.
	 * </p>
	 * <p>
	 * This constructor is designed to be called whenever a new Community is created
	 * by a user. This constructor saves the newly created object.
	 * </p>
	 * <p>
	 * Currently, communities do not need to be registered with the world, but if
	 * they need to be in the future, this constructor will register the newly
	 * created community on its own.
	 * </p>
	 * 
	 * @param world The world that the community belongs to.
	 * @param owner The owner of the community.
	 * @param name  The name of the community.
	 */
	public ServerCommunityImpl(final ServerWorldImpl world, final ServerUserImpl owner, String name) {
		super(world);
		this.owner = owner;
		this.name = name;
		participants = new HashSet<>();
		participants.add(owner);
		save();
	}

	public ServerCommunityImpl(final ServerWorldImpl world, final JSONObject object) {
		super(object, world);
		name = getString(object, NAME_KEY);
		final JSONArray participants = getArray(object, PARTICIPANTS_KEY);
		this.participants = new HashSet<>(participants.size());
		for (final JSONValue o : participants)
			if (o instanceof JSONString) {
				final String hex = ((JSONString) o).getValue();
				try {
					this.participants.add(world.getRegistry().getUser(GID.fromHex(hex)));
				} catch (final NumberFormatException e) {
					throw new IllegalArgumentException(
							"Invalid GID found in participants list for ServerCommunity.\n\tCommunity: " + getGID()
									+ "\n\tViolating GID: " + hex);
				}
			} else
				throw new IllegalArgumentException(
						"Community snapshot contains invalid value in participants array: " + o);

		final JSONArray threads = getArray(object, THREADS_KEY);
		for (final JSONValue o : threads)
			if (o instanceof JSONString) {
				final String hex = ((JSONString) o).getValue();
				try {
					this.threads.add(world.getRegistry().getCommunityThread(GID.fromHex(hex)));
				} catch (final NumberFormatException e) {
					throw new IllegalArgumentException(
							"Invalid GID found in threads list for ServerCommunity.\n\tCommunity: " + getGID()
									+ "\n\tViolating GID: " + hex);
				}
			} else
				throw new IllegalArgumentException("Community snapshot contains invalid value in threads array: " + o);

		owner = world.getRegistry().getUser(getGID(object, OWNER_KEY));
	}

	/**
	 * <p>
	 * Adds the specified user to this community, if it is not already in this
	 * community. If it is, this method does nothing.
	 * </p>
	 * <p>
	 * This method saves this object and then saves the user after it finishes
	 * adding the user to this community.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void addUser(final ServerUser user) {
		if (participants.contains(user))
			return;
		final ServerUserImpl u = (ServerUserImpl) user;
		participants.add(u);
		u.communities.add(this);

		save();
		u.save();
	}

	/**
	 * <p>
	 * Changes the owner of this community to the specified user. If the user is not
	 * already in this community, this method does nothing. If the specified user
	 * already owns this community, this method does nothing.
	 * </p>
	 * <p>
	 * This method saves this community after it changes the owner.
	 * </p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void changeOwner(final ServerUser newOwner) {
		if (!participants.contains(newOwner))
			return;
		if (owner == newOwner)
			return;
		owner = (ServerUserImpl) newOwner;
		save();
	}

	/**
	 * Creates a community thread by name but does not register it to this
	 * {@link ServerCommunityImpl}.
	 *
	 * @param name The name of this {@link ServerCommunityThreadImpl}.
	 * @return The new {@link ServerCommunityThreadImpl}.
	 */
	private ServerCommunityThreadImpl createLooseThread(final String name) {
		return new ServerCommunityThreadImpl(this, name);
	}

	@Override
	public ServerCommunityThread createThreadAtIndex(final String name, final int index) throws IndexOutOfBoundsException {
		final ServerCommunityThreadImpl thread = createLooseThread(name);
		threads.add(index, thread);
		thread.save();
		save();
		return thread;
	}

	@Override
	public void delete() {
		for (final ServerUserImpl u : participants) {
			u.communities.remove(this);
			u.save();
		}
		while (!threads.isEmpty())
			threads.get(0).deleteAsChild();
		deleteFile();
	}

	@Override
	public InputStream getBackground() throws FileNotFoundException {
		return getBackgroundFile().isFile() ? InputStream.fromJavaInputStream(new FileInputStream(getBackgroundFile()))
				: null;
	}

	@Override
	public InputStream getIcon() throws FileNotFoundException {
		return getIconFile().isFile() ? InputStream.fromJavaInputStream(new FileInputStream(getIconFile())) : null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ServerUser getOwner() {
		return owner;
	}

	@Override
	public List<ServerCommunityThread> getThreads() {
		return Collections.unmodifiableList(threads);
	}

	@Override
	public Set<? extends ServerUser> getUsers() {
		return Collections.unmodifiableSet(participants);
	}

	@Override
	public void removeThread(final ServerCommunityThread thread) {
		if (threads.contains(thread))
			thread.delete();
	}

	@Override
	public void removeUser(final ServerUser user) {
		if (owner.equals(user))// If the owner is trying to leave, do nothing.
			return;
		participants.remove(user);
		ServerUserImpl userImpl = (ServerUserImpl) user;
		userImpl.communities.remove(this);
		userImpl.save();
		save();
	}

	@Override
	public void rename(final String newName) {
		name = newName;
		save();
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		// IMPLEMENT Auto-generated method stub
		super.restore(snap);
	}

	@Override
	public JSONObject snapshot() {
		final JSONObject snapshot = super.snapshot().put(NAME_KEY, name);
		snapshot.put(PARTICIPANTS_KEY,
				new JSONArray(JavaTools.mask(participants, a -> new JSONString(a.getGID().getHex()))));
		snapshot.put(THREADS_KEY, new JSONArray(JavaTools.mask(threads, a -> new JSONString(a.getGID().getHex()))));
		snapshot.put(OWNER_KEY, owner.getGID().getHex());
		return snapshot;
	}

	@Override
	public File getStorageFile() {
		return new File(getWorld().getCommunityPath(), getGID().getHex() + ".aso");
	}

	@Override
	public String toString() {
		return getName() + "[owner=" + getOwner() + ']';
	}

	private File getBackgroundFile() {
		return new File(getAssetDirectory(), "background.png");
	}

	private File getIconFile() {
		return new File(getAssetDirectory(), "icon.png");
	}

	@Override
	public void setBackground(byte[] icon)
			throws FileNotFoundException, IOException, UnknownCommStateException, BlockException {
		File file = getBackgroundFile();
		if (icon == null)
			Files.deleteIfExists(file.toPath());
		else {
			file.getParentFile().mkdirs();
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(icon);
			}
		}
	}

	@Override
	public void setIcon(byte[] icon) throws UnknownCommStateException, IOException, BlockException {
		File file = getIconFile();
		if (icon == null)
			Files.deleteIfExists(file.toPath());
		else {
			file.getParentFile().mkdirs();
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(icon);
			}
		}
	}

	/**
	 * 
	 */
	@Override
	public File getAssetDirectory() {
		return new File(getWorld().getRootDirectory(),
				"assets/" + ServerWorldImpl.COMMUNITIES_STORAGE_LOCATION_SUFFIX + '/' + getGID().getHex());
	}

}