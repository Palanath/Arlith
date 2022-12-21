package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetOwnUserRequest;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class GetOwnUserRequestHandler extends SimpleRequestHandler<GetOwnUserRequest> {

	@Override
	protected void handle(final GetOwnUserRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		client.sendResult(RequestHandlerUtils.fromUser(client.getUser()));
	}

	public GetOwnUserRequestHandler() {
		super(GetOwnUserRequest::new);
	}
}
