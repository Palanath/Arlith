package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.SetStatusRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

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
