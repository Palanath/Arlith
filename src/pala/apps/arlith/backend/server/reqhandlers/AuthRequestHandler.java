package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.AuthError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.common.protocol.types.AuthProblemValue;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.systems.EventConnectionImpl;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

/**
 * <p>
 * Handles a {@link AuthRequest}, which is an attempt to log a connection in via
 * {@link AuthToken}.
 * </p>
 * 
 * @author Palanath
 *
 */
public final class AuthRequestHandler extends SimpleRequestHandler<AuthRequest> {

	@Override
	protected void handle(final AuthRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		// If the client is sending this after already being an authorized
		// RequestConnection, reject them. (Why would they do this lol?)
		if (client.isAuthorized()) {
			client.sendError(new RestrictedError("This connection is already authorized."));
			return;
		}

		// Get the user ID of the account that matches the specified auth token. Returns
		// null if the token is invalid (i.e., if it does not point to any user).
		final GID acc = client.getServer().getAuthSystem().getUser(r.getAuthToken().getToken());

		if (acc != null) {
			// We get here if the auth token that the connection specified is valid (so we
			// know what user is trying to log in over this connection).

			// Send a log message noting that someone's connection is logging in.
			String userTag = client.getServer().getWorld().getUserByID(acc).getTag();
			ArlithServer.getThreadLogger().std(userTag + " is authenticating this connection via token.");
			// Regardless of the outcome, quickly set the thread's purpose; update it to
			// include the user's tag.
			ArlithServer.changeThreadLoggerPurpose(userTag);

			// Check if they want an event connection or request connection.
			if (r.getEventConnection().is()) {
				// They want this to become an event connection (meaning the server will send
				// events over this).

				// We want to create a new EventConnection object off of this connection's
				// underlying "Connection" object. Hence the "new
				// EventConnectionImpl(client.getConnection(), acc)". The
				// "client.getConnection()" gets the underlying "Connection" object (used to
				// actually send data back and forth) and gives it to the EventConnectionImpl
				// constructor.
				//
				// We then want to register that EventConnection with the event system, which we
				// do with the call to "registerClient(...)" below. Registration is how the
				// server keeps track of the event connection for the user that's logging in.
				//
				// Whenever an event is fired (e.g., message received) and needs to be sent to a
				// specific user, the server queries all of the event connections currently
				// active and logged in for that user and sends it to all of them.
				client.getServer().getEventSystem()
						.registerClient(new EventConnectionImpl(client.getConnection(), acc));

				// The server tries to wait for incoming requests on this thread in a while
				// loop. We need to disable this, because the client wants to repurpose this
				// connection for having the server SEND events over it. The server will not
				// RECEIVE requests over this connection anymore.
				client.stopListening();
			} else
				client.authorize(acc);
			// Tell client that we're done. :^)
			client.sendResult(new CompletionValue());
		} else
			client.sendError(new AuthError(AuthProblemValue.INVALID_TOKEN));
	}

	public AuthRequestHandler() {
		super(AuthRequest::new);
	}
}
