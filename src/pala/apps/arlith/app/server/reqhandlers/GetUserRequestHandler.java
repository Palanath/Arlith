package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetUserRequest;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class GetUserRequestHandler extends SimpleRequestHandler<GetUserRequest> {

	@Override
	protected void handle(final GetUserRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerUser user = client.getWorld().getUserByID(r.getUser().getGid());
		if (user == null) {
			client.sendError(new ObjectNotFoundError(r.getUser(), "No user found with provided GID."));
			return;
		}
		client.sendResult(RequestHandlerUtils.fromUser(user));
	}

	public GetUserRequestHandler() {
		super(GetUserRequest::new);
	}
}
