package pala.apps.arlith.frontend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.GetUserRequest;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;

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
