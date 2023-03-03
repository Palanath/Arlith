package pala.apps.arlith.backend.server.contracts.serversystems;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.types.CommunicationProtocolType;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.server.contracts.world.ServerWorld;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

/**
 * <h1>Overview</h1>
 * <p>
 * Represents a client connection dedicated to making server requests. Clients
 * send the server requests (analogous to function calls on an API) over a
 * request connection, and the server sends back responses. This type represents
 * a request connection with the client, on the server's end.
 * </p>
 * <p>
 * Requests are <i>client-initiated</i>, meaning that the server <i>listens</i>
 * for incoming requests and handles them as they come in. The server only sends
 * data over the connection in <i>response</i> to the client's request queries.
 * </p>
 * <h1>Purpose</h1>
 * <p>
 * {@link RequestConnection}s store metadata alongside a connection that the
 * server can use. They can be <i>authenticated</i>, meaning that a client has
 * sent a form of log-in request, identifying who it is to the server, or
 * <i>unauthenticated</i>.
 * </p>
 * <p>
 * {@link RequestConnection}s also store a flag determining whether they should
 * be <i>listened to</i> by the server. When this flag is <code>true</code>, the
 * server should listen for incoming requests on the underlying
 * {@link Connection}.
 * </p>
 * <p>
 * {@link RequestConnection}s <span style="color: red;">might not be
 * authenticated</span> by the time they are being used by a
 * {@link RequestHandler}. {@link RequestHandler}s should always check the
 * associated metadata
 * </p>
 * <p>
 * A {@link RequestConnection} should stop being listened to when it's closed.
 * Also note that {@link RequestConnection}s do not get closed automatically
 * when their underlying {@link Connection} fails or is closed;
 * {@link #closeConnection()} must be called on the {@link RequestConnection} to
 * successfully close it (although this can safely be called after the
 * underlying {@link Connection} closes for whatever reason).
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestConnection extends ClientConnection {

	/**
	 * <p>
	 * Determines whether the a user at the other end of this connection has
	 * authorized itself.
	 * </p>
	 * <p>
	 * If this method returns <code>false</code>, then {@link #getUserID()} will
	 * return <code>null</code>. This is because authorization happens at the same
	 * time that the user at the other end of a connection is determined.
	 * </p>
	 * 
	 * @return <code>true</code> if this connection is authorized (meaning a user
	 *         has logged in correctly over it), <code>false</code> otherwise.
	 */
	boolean isAuthorized();

	/**
	 * <p>
	 * Authorizes this {@link RequestConnection} under the specified user's
	 * {@link GID}. This is meant to be called by {@link RequestHandler}s that log
	 * the connection in under a user.
	 * </p>
	 * <p>
	 * This method basically associates the metadata of the user (via the
	 * {@link GID}) on the client-side of the connection with this object so that
	 * the server can access it.
	 * </p>
	 * 
	 * @param userID The {@link GID} of the user logged in over this connection.
	 */
	void authorize(GID userID);

	/**
	 * Returns the ID of the user that's connected through this client if the user
	 * at the other end of this connection has performed the authentication
	 * handshake and successfully logged in, identifying itself. Otherwise, this
	 * method will return <code>null</code>.
	 * 
	 * @return The {@link GID} of the user at the other end of this connection, or
	 *         <code>null</code> if not known.
	 */
	@Override
	GID getUserID();

	/**
	 * <p>
	 * Stops this {@link RequestConnection} from being listened to without closing
	 * the underlying connection. This is essentially the same as closing the
	 * connection, except that the underlying {@link Connection} can still be used.
	 * The {@link RequestConnection} object can be discarded after this is called
	 * (but the underlying {@link Connection} object will still be active and would
	 * need to be closed independently).
	 * </p>
	 * <p>
	 * The primary purpose of this method is so that {@link RequestConnection}s can
	 * be transformed into {@link EventConnection}s.
	 * </p>
	 */
	void stopListening();

	/**
	 * <p>
	 * Returns whether or not the listening thread should listen for incoming
	 * requests on this {@link RequestConnection}. This is typically used as the
	 * condition in a while loop construct that handles incoming requests so long as
	 * the {@link RequestConnection} is {@link #active()}.
	 * </p>
	 * <p>
	 * This method will return <code>false</code> if {@link #stopListening()} has
	 * been called on this {@link RequestConnection} or if
	 * {@link #closeConnection()} has been called on this {@link RequestConnection}.
	 * </p>
	 * 
	 * @return <code>true</code> if the {@link RequestConnection} is active and
	 *         requests should be listened for from it.
	 */
	boolean active();

	/**
	 * <p>
	 * Delegate method to get the user on the other end of this
	 * {@link RequestConnection}. If there is no known user yet (i.e.
	 * {@link #isAuthorized()} returns <code>false</code>, this method returns
	 * <code>null</code>.
	 * </p>
	 * <p>
	 * This method will never return <code>null</code> if {@link #isAuthorized()}
	 * returns <code>true</code>.
	 * </p>
	 * 
	 * @return The {@link ServerUser} or <code>null</code>.
	 */
	default ServerUser getUser() {
		return getUserID() == null ? null : getWorld().getUserByID(getUserID());
	}

	/**
	 * Returns the {@link RequestSystem} that tracks and handles this
	 * {@link RequestConnection}.
	 * 
	 * @return The {@link RequestSystem} that manages this connection.
	 */
	RequestSystem getManager();

	/**
	 * <p>
	 * Returns the server that this {@link RequestConnection} functions under. The
	 * server is obtained through the {@link RequestSystem} that this client is
	 * managed by through a call to {@link RequestSystem#getServer()}.
	 * </p>
	 * <p>
	 * This is a delegate method.
	 * </p>
	 * 
	 * @return The server that this {@link RequestConnection} works within. The user
	 *         whose request connection is represented by this object belongs to the
	 *         returned server.
	 */
	default ArlithServer getServer() {
		return getManager().getServer();
	}

	/**
	 * <p>
	 * Returns the {@link ServerWorld} to be used for API calls by request-handling
	 * code. Such code can call {@link ServerWorld} functions and access objects in
	 * the server world to perform the requests invoked by the client connected over
	 * this {@link RequestConnection}.
	 * </p>
	 * <p>
	 * This method delegates to calling {@link ArlithServer#getWorld()} on the
	 * returned value from {@link #getServer()}. This is a delegate method.
	 * </p>
	 * 
	 * @return The server's world.
	 */
	default ServerWorld getWorld() {
		return getServer().getWorld();
	}

	/**
	 * <p>
	 * Stops this {@link RequestConnection} from being listened to
	 * ({@link #stopListening()}) and then closes the underlying {@link Connection}.
	 * </p>
	 */
	@Override
	default void closeConnection() {
		stopListening();
		ClientConnection.super.closeConnection();
	}

	/**
	 * Sends a result to the client. This is meant to be performed by
	 * request-handling code on the server to send results back to the client when
	 * handling a request succeeds.
	 * 
	 * @param result The result to send over the connection.
	 * @throws UnknownCommStateException If the connection went into an unknown
	 *                                   state as a result of this method call and
	 *                                   so the connection was closed.
	 * @author Palanath
	 */
	default void sendResult(CommunicationProtocolType result) throws UnknownCommStateException {
		result.send(getConnection());
	}

	/**
	 * Sends an error to the client. This is meant to be performed by
	 * request-handling code on the server to send errors when handling a request
	 * fails. This method will also print the specified error's stacktrace.
	 * 
	 * @param error The error that will be sent to the client. It also gets logged
	 *              to the console.
	 * @throws UnknownCommStateException If the connection went into an unknown
	 *                                   state as a result of this method call and
	 *                                   so the connection was closed.
	 */
	default void sendError(CommunicationProtocolError error) throws UnknownCommStateException {
		ArlithServer.getThreadLogger().err("Sending an error to the client: " + error.getClass().getSimpleName());
		ArlithServer.getThreadLogger().err("vvv Error Message Below vvv");
		ArlithServer.getThreadLogger().err(error);
		error.send(getConnection());
	}

}
