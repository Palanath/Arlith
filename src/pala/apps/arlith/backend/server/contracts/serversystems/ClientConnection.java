package pala.apps.arlith.backend.server.contracts.serversystems;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;

/**
 * Represents a {@link ClientConnection} from the perspective of the server. A
 * {@link ClientConnection} encapsulates a {@link Connection} object that can be
 * used to communicate with the entity on the other end, possibly in addition to
 * some auxiliary information, such as what {@link ServerUser} is on the other end
 * of the connection, if such has been determined yet, or whether such a user
 * has authorized themselves, etc.
 * 
 * @author Palanath
 *
 */
public interface ClientConnection {
	/**
	 * Returns the {@link Connection} object over which client communication occurs.
	 * This will not return <code>null</code>, even after the {@link Connection} is
	 * closed.
	 * 
	 * @return The {@link Connection} that underlies this {@link RequestConnection}.
	 */
	Connection getConnection();

	/**
	 * Closes the connection backing this {@link ClientConnection}. This method also
	 * performs any cleanup required to shut down this {@link ClientConnection}.
	 */
	default void closeConnection() {
		getConnection().close();
	}

	/**
	 * Returns the ID of the user that is connected through this client, if known.
	 * 
	 * @return The ID of the user connected through this client.
	 */
	GID getUserID();
}
