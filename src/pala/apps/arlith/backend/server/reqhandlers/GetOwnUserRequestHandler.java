package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.GetOwnUserRequest;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

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
