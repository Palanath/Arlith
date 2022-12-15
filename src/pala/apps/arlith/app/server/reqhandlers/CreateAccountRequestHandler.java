package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.authentication.AuthToken;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.api.communication.protocol.types.AuthTokenValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.app.server.systems.EventConnectionImpl;

public final class CreateAccountRequestHandler extends SimpleRequestHandler<CreateAccountRequest> {

	@Override
	protected void handle(final CreateAccountRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {

		if (client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			AuthToken token;
			ServerUser user;
			user = client.getWorld().createUserWithEmailAndPhone(r.username(), r.getPassword(), r.emailAddress(),
					r.phoneNumber());
			token = client.getServer().getAuthSystem().login(user);
			// Register an Event Client wrapping this same connection to the event handler.
			client.getServer().getEventSystem()
					.registerClient(new EventConnectionImpl(client.getConnection(), user.getGID()));
			client.sendResult(new AuthTokenValue(token));
			client.stopListening();
//			BasicRequestResolver.log("The user: [" + client.getAccount().getUsername()
//					+ "] just created an account {EVENT_HANDLER} from IP: ["
//					+ client.getConnectionInfo().getInetAddress() + "] on remote port: ["
//					+ client.getConnectionInfo().getPort() + "].");
		}
	}

	public CreateAccountRequestHandler() {
		super(CreateAccountRequest::new);
	}
}
