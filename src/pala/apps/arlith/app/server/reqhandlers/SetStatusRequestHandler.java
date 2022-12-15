package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.SetStatusRequest;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;

public final class SetStatusRequestHandler extends SimpleRequestHandler<SetStatusRequest> {

	@Override
	protected void handle(final SetStatusRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
//		try {
//			client.getUser().setStatus(r.getStatus().getValue());
//		} catch (final IOException e) {
//			throw new RuntimeException(e);
//		}
		client.sendResult(new CompletionValue());
	}

	public SetStatusRequestHandler() {
		super(SetStatusRequest::new);
	}
}
