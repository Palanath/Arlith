package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.app.server.systems.EventConnectionImpl;
import pala.apps.arlith.backend.communication.authentication.AuthToken;
import pala.apps.arlith.backend.communication.protocol.errors.LoginError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.LoginRequest;
import pala.apps.arlith.backend.communication.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.communication.protocol.types.LoginProblemValue;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class LoginRequestHandler extends SimpleRequestHandler<LoginRequest> {

	@Override
	protected void handle(final LoginRequest r, final RequestConnection client) throws UnknownCommStateException {
		if (client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			// TODO Kick out old client connections.
			AuthToken at;
			ServerUser user;
			if (r.getEmail() != null) {
				if ((user = client.getWorld().getUserByEmail(r.getEmail().getValue())) == null) {
					client.sendError(new LoginError(LoginProblemValue.INVALID_EM));
					return;
				}
			} else if (r.getPhone() != null) {
				if ((user = client.getWorld().getUserByPhone(r.getPhone().getValue())) == null) {
					client.sendError(new LoginError(LoginProblemValue.INVALID_PH));
					return;
				}
			} else if ((user = client.getServer().getWorld().getUserByUsername(r.getUsername().getValue(),
					r.getDisc().getValue())) == null) {
				client.sendError(new LoginError(LoginProblemValue.INVALID_UN));
				return;
			} else if (!user.getPassword().equals(r.getPassword())) {
				client.sendError(new LoginError(LoginProblemValue.INVALID_PW));
				return;
			}
			at = client.getServer().getAuthSystem().login(user);
			client.getServer().getEventSystem()
					.registerClient(new EventConnectionImpl(client.getConnection(), user.getGID()));
			client.sendResult(new AuthTokenValue(at));
			client.stopListening();
//			BasicRequestResolver.log("The user: [" + client.getAccount().getUsername()
//					+ "] logged in {EVENT_HANDLER}. IP: [" + client.getConnectionInfo().getInetAddress() + ':'
//					+ client.getConnectionInfo().getPort() + "].");
		}
	}

	public LoginRequestHandler() {
		super(LoginRequest::new);
	}
}
