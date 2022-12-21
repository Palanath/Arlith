package pala.apps.arlith.frontend.server.reqhandlers;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.LoginRequest;
import pala.apps.arlith.backend.common.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;
import pala.apps.arlith.frontend.server.systems.EventConnectionImpl;

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
