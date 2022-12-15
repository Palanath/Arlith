package pala.apps.arlith.app.server.world;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.app.server.contracts.coldstorage.Snapshottable;
import pala.apps.arlith.app.server.contracts.world.ServerObject;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONObject;

public class ServerObjectImpl implements ServerObject, Snapshottable {
	public static final String GID_KEY = "ID";

	public static void checkKey(final JSONObject object, final String key) {
		if (!object.containsKey(key))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"" + key + "\".");
	}

	public static JSONArray getArray(final JSONObject object, final String key) {
		checkKey(object, key);
		return (JSONArray) object.get(key);
	}

	@Override
	public int hashCode() {
		return 31 * (31 + ((gid == null) ? 0 : gid.hashCode())) + ((owner == null) ? 0 : owner.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ServerObjectImpl))
			return false;
		ServerObjectImpl other = (ServerObjectImpl) obj;
		if (gid == null) {
			if (other.gid != null)
				return false;
		} else if (!gid.equals(other.gid))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	/**
	 * <p>
	 * Gets and returns a {@link GID} stored in hex format from the provided entry
	 * in the provided {@link JSONObject}. If the provided {@link JSONObject} does
	 * not contain such a valid {@link GID} under the provided key, this method
	 * throws an {@link IllegalArgumentException}.
	 * </p>
	 * <p>
	 * The purpose of this function is entirely to convenience
	 * {@link ServerObjectImpl}'s, (and its subclasses'), loading references to other
	 * {@link ServerObjectImpl}s through their snapshot loading constructors (such as
	 * {@link #ServerObjectImpl(JSONObject, ServerWorldImpl)}).
	 * </p>
	 * <p>
	 * This method can and may also be used by {@link #restore(JSONObject)} to load
	 * properties from the snapshot while also conveniently checking if such
	 * properties exist.
	 * </p>
	 *
	 * @param object The object to load the {@link GID} from.
	 * @param key    The key of the {@link GID} to load.
	 * @return The loaded {@link GID}.
	 * @throws IllegalArgumentException If a {@link GID} could not be loaded from
	 *                                  the specified key.
	 */
	public static GID getGID(final JSONObject object, final String key) throws IllegalArgumentException {
		if (!object.containsKey(key))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"" + key + "\".");
		final String gid = object.getString(key);
		try {
			return GID.fromHex(gid);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Snapshot does not contain a valid GID (under key \"" + key + "\").");
		}
	}

	public static int getInt(final JSONObject object, final String key) throws IllegalArgumentException {
		checkKey(object, key);
		return object.getInt(key);
	}

	public static long getLong(final JSONObject object, final String key) throws IllegalArgumentException {
		if (!object.containsKey(key))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"" + key + "\".");
		return object.getLong(key);
	}

	public static JSONObject getObject(final JSONObject object, final String key) {
		checkKey(object, key);
		return (JSONObject) object.get(key);
	}

	public static String getString(final JSONObject object, final String key) throws IllegalArgumentException {
		if (!object.containsKey(key))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"" + key + "\".");
		return object.getString(key);
	}

	private final GID gid;

	private final ServerWorldImpl owner;

	public ServerObjectImpl(final ServerWorldImpl owner) {
		this.owner = owner;
		gid = owner.getGidProvider().generateGid();
	}

	/**
	 * <p>
	 * Constructs this {@link ServerObjectImpl} from the provided {@link JSONObject}
	 * snapshot and under the provided {@link ServerWorldImpl} owner. This constructor
	 * registers this object with the {@link ServerWorldImpl}'s {@link WorldRegistry}
	 * (see {@link ServerWorldImpl#getRegistry()}), and is designed to be used when
	 * loading objects from the filesystem.
	 * </p>
	 * <p>
	 * This constructor loads this {@link ServerObjectImpl}'s {@link #gid} (see
	 * {@link #getGID()}) from the provided snapshot. If the snapshot does not
	 * contain a valid {@link GID} under the key, {@link #GID_KEY}, this constructor
	 * throws an {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param snap  A snapshot of this {@link ServerObjectImpl} object to load from.
	 * @param owner The world that owns this {@link ServerObjectImpl}.
	 * @throws IllegalArgumentException If the provided snapshot does not contain a
	 *                                  valid {@link GID} under the key
	 *                                  {@link #GID_KEY}.
	 */
	public ServerObjectImpl(final JSONObject snap, final ServerWorldImpl owner) throws IllegalArgumentException {
		if (!snap.containsKey(GID_KEY))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"ID\".");
		final String gid = snap.getString(GID_KEY);
		try {
			this.gid = GID.fromHex(gid);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Snapshot does not contain a valid GID (under key \"ID\").");
		}
		(this.owner = owner).getRegistry().register(this);
	}

	@Override
	public GID getGID() {
		return gid;
	}

	@Override
	public ServerWorldImpl getWorld() {
		return owner;
	}

	@Override
	public void restore(final JSONObject snap) throws IllegalArgumentException {
		if (!snap.containsKey(GID_KEY))
			throw new IllegalArgumentException("Snapshot does not contain entry with key \"ID\".");
		final String gid = snap.getString(GID_KEY);
		GID g;
		try {
			g = GID.fromHex(gid);
		} catch (final Exception e) {
			throw new IllegalArgumentException("Snapshot does not contain a valid GID (under key \"ID\").");
		}
		if (!g.equals(this.gid))
			throw new IllegalArgumentException(
					"Provided snapshot does not represent this same ServerObjectImpl. (GID of this object does not match GID stored in snapshot.)");
	}

	@Override
	public JSONObject snapshot() {
		return new JSONObject().put(GID_KEY, gid.getHex());
	}

}
