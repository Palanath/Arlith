package pala.apps.arlith.frontend.server.contracts.serversystems;

import java.util.List;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.connections.networking.Connection;
import pala.apps.arlith.frontend.server.ArlithServer;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;

/**
 * <p>
 * A {@link RequestSystem} is the structure that manages and handles everything
 * directly having to do with <i>requests</i> on the server.
 * {@link RequestSystem}s both map incoming requests to the appropriate
 * {@link RequestHandler} (see {@link RequestMapper}; every
 * {@link RequestSystem} is also a {@link RequestMapper}), but also keep track
 * of all {@link RequestConnection}s and that each {@link ServerUser} is connected
 * via. This allows {@link RequestHandler}s to be able to find which
 * {@link RequestConnection}s a {@link ServerUser} is connected over, and, for
 * example, close them all if the user is performing a total log-out operation
 * (not implemented at the time of this writing, but useful for example
 * purposes).
 * </p>
 * <p>
 * Note that {@link RequestConnection}s may exist that are <i>not</i> tracked by
 * the server's {@link RequestSystem}. These {@link RequestConnection}s are not
 * tracked because they have not authenticated themselves yet, so the server
 * does not know what user they belong to (if any).
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestSystem extends RequestMapper {
	/**
	 * Gets the server that owns this {@link RequestSystem}.
	 * 
	 * @return The server that owns this {@link RequestSystem}.
	 */
	ArlithServer getServer();

	/**
	 * <p>
	 * Called by the server to register a new {@link RequestConnection} connection
	 * that has successfully logged in as a user. This class keeps track of logged
	 * in {@link RequestConnection}s, as per the server's registration of them. The
	 * {@link RequestSystem} only keeps track of these; it does not do anything with
	 * them, and this {@link RequestSystem} can delegate incoming requests (via
	 * {@link #handleRequest(RequestConnection)}) on <i>any</i>
	 * {@link RequestConnection}; not just one that is "registered." The
	 * {@link RequestConnection} is registered on the current {@link Thread} (see
	 * {@link Thread#currentThread()}).
	 * </p>
	 * <p>
	 * This should be called on the thread that listens to incoming requests.
	 * </p>
	 * 
	 * @param connection The actual {@link Connection}.
	 */
	void registerAuthenticatedRequestClient(RequestConnection connection);

	/**
	 * <p>
	 * Called by the server to unregister a {@link RequestConnection} after it is
	 * closed or when it is being turned into an {@link EventConnection} (the
	 * {@link RequestConnection} object is being disposed of but a new
	 * {@link EventConnection} is being created on the underlying
	 * {@link Connection}). This removes the {@link RequestConnection} from being
	 * tracked by this {@link RequestSystem}.
	 * </p>
	 * <p>
	 * If the specified {@link RequestConnection} is not already being tracked by
	 * this {@link RequestSystem} then this method does nothing.
	 * </p>
	 * 
	 * @param connection The {@link RequestConnection} to remove.
	 */
	void unregisterRequestClient(RequestConnection connection);

	/**
	 * <p>
	 * Returns an unmodifiable {@link List} containing the
	 * {@link RequestConnection}s that the user with the specified {@link GID}
	 * currently has connected to the server. The list is updated whenever a new
	 * incoming {@link RequestConnection} connection is authenticated (handled by
	 * the server) and then registered, and the list is also updated whenever a
	 * client is dropped.
	 * </p>
	 * 
	 * @param userID The {@link GID} of the user to get the connected
	 *               {@link RequestConnection}s of.
	 * @return An unmodifiable {@link List} of the {@link RequestConnection}s. The
	 *         list reflects any updates that are made.
	 */
	List<RequestConnection> getAuthenticatedRequestClients(GID userID);

}
