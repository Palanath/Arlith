package pala.apps.arlith.frontend.server.contracts.world;

import java.util.Collections;
import java.util.List;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.frontend.server.ArlithServer;

public interface ServerObject extends Comparable<ServerObject> {
	/**
	 * Returns this {@link ServerObject}'s globally unique {@link GID}.
	 *
	 * @return This object's {@link GID}.
	 */
	GID getGID();

	@Override
	default int compareTo(ServerObject o) {
		return getGID().compareTo(o.getGID());
	}

	/**
	 * Gets the {@link ServerWorld} that this {@link ServerObject} belongs to.
	 *
	 * @return The {@link ServerWorld} that this {@link ServerObject} belongs to.
	 */
	ServerWorld getWorld();

	/**
	 * Returns the server that possesses this {@link ServerObject}.
	 *
	 * @return The server that this {@link ServerObject} functions under. The server is
	 *         accessed through {@link #getWorld()}'s {@link ServerWorld#getServer()}.
	 */
	default ArlithServer server() {
		return getWorld().getServer();
	}

	/**
	 * <p>
	 * Returns a new, <i>faux</i> {@link ServerObject}. The returned {@link ServerObject}
	 * has no corresponding {@link #getWorld() world} (i.e. {@link #getWorld()}
	 * returns <code>null</code>), and returns the {@link GID} specified.
	 * </p>
	 * <p>
	 * This function was designed to generate {@link ServerObject}s that are useful for
	 * calls to {@link Collections#binarySearch(java.util.List, Object)}; the return
	 * value of this method is a {@link ServerObject} that possesses the specified
	 * {@link GID}, so (although it is not a real object), it can be compared with
	 * real {@link ServerObject}s, like a {@link ServerMessage}, and can therefore be used
	 * as a dummy "key" when searching for a {@link ServerObject} with a specified
	 * {@link GID} inside a sorted {@link List}.
	 * </p>
	 * <p>
	 * Care should be taken to be able to assert that the underyling implementation
	 * of {@link Collections#binarySearch(List, Object)} only uses the
	 * {@link #compareTo(ServerObject) comparator functionality} to compare objects (as
	 * such is not specified clearly in the documentation, although it is likely
	 * that this is not the case).
	 * </p>
	 * 
	 * @param gid The {@link GID} of the new, faux object.
	 * @return The new, faux {@link ServerObject} with the specified {@link GID}.
	 */
	static ServerObject fauxComparableObject(GID gid) {
		return new ServerObject() {

			@Override
			public ServerWorld getWorld() {
				return null;
			}

			@Override
			public GID getGID() {
				return gid;
			}
		};
	}
}
