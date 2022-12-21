package pala.apps.arlith.app.server.world;

import java.io.File;

import pala.apps.arlith.app.server.contracts.world.ServerDirectThread;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.libs.generic.json.JSONObject;

final class ServerDirectThreadImpl extends ServerThreadImpl implements ServerDirectThread {

	private static final String STARTER_KEY = "starter", RECEIVER_KEY = "receiver";
	private final ServerUser starter, receiver;

	/**
	 * <p>
	 * Constructs a {@link ServerDirectThreadImpl} between the specified
	 * {@link ServerUser}s. The <code>starter</code> is the user that has initiated the
	 * creation of this direct thread, and the <code>receiver</code> is the other
	 * user participating in the direct thread. This constructor is designed to be
	 * called when a user begins a direct message thread with another user.
	 * </p>
	 * <p>
	 * This constructor saves the direct thread to the filesystem.
	 * </p>
	 *
	 * @param starter     The user that initiated the creation of this
	 *                    {@link ServerDirectThreadImpl}.
	 * @param receiver    The other user participating in this
	 *                    {@link ServerDirectThreadImpl}.
	 * @param serverWorldImpl The world that the thread is a part of.
	 */
	public ServerDirectThreadImpl(final ServerWorldImpl serverWorldImpl, final ServerUser starter, final ServerUser receiver) {
		super(serverWorldImpl);
		this.starter = starter;
		this.receiver = receiver;
		save();
	}

	public ServerDirectThreadImpl(final ServerWorldImpl serverWorldImpl, final JSONObject snapshot) {
		super(serverWorldImpl, snapshot);
		ServerUserImpl i = (ServerUserImpl) (starter = world.getRegistry().getUser(getGID(snapshot, STARTER_KEY)));
		i.directThreads.put(receiver = world.getRegistry().getUser(getGID(snapshot, RECEIVER_KEY)), this);
		((ServerUserImpl) receiver).directThreads.put(i, this);
	}

	@Override
	public ServerUser getReceiver() {
		return receiver;
	}

	@Override
	public ServerUser getStarter() {
		return starter;
	}

	@Override
	public File getStorageFile() {
		return new File(world.getDirectThreadPath(), getGID().getHex() + ".aso");
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		super.restore(snap);// Restore super properties.
		GID gid;
		if (!snap.containsKey(RECEIVER_KEY))
			throw new IllegalArgumentException("Thread snapshot missing second user's ID (\"recipient\").");
		if (!snap.containsKey(STARTER_KEY))
			throw new IllegalArgumentException("Thread snapshot missing first user's ID (\"starter\").");

		try {
			gid = GID.fromHex(snap.getString(RECEIVER_KEY));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Snapshot does not contain a valid second user's GID (\"recipient\").");
		}
		if (!gid.equals(receiver.getGID()))
			throw new IllegalAccessError("The participants of a direct thread cannot be changed. (\"recipient\")");

		try {
			gid = GID.fromHex(snap.getString(STARTER_KEY));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Snapshot does not contain a valid second user's GID (\"starter\").");
		}
		if (!gid.equals(starter.getGID()))
			throw new IllegalAccessError("The participants of a direct thread cannot be changed. (\"starter\")");
	}

	@Override
	public JSONObject snapshot() {
		// Currently no changeable properties to snapshot. Snapshot should contain GID +
		// starter ID and receiver ID.
		return super.snapshot().put(STARTER_KEY, starter.getGID().getHex()).put(RECEIVER_KEY,
				receiver.getGID().getHex());

	}

}