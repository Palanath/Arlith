package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.ChangeEmailRequest;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;

public final class ChangeEmailRequestHandler extends SimpleRequestHandler<ChangeEmailRequest> {

	@Override
	protected void handle(ChangeEmailRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			client.getUser().changeEmail(r.email());
			client.sendResult(new CompletionValue());
		}
	}

	public ChangeEmailRequestHandler() {
		super(ChangeEmailRequest::new);
	}
}
