package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.backend.communication.protocol.types.CompletionValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class ChangePhoneNumberRequestHandler extends SimpleRequestHandler<ChangePhoneNumberRequest> {

	@Override
	protected void handle(ChangePhoneNumberRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			client.getUser().changePhone(r.phoneNumber());
			client.sendResult(new CompletionValue());
		}
	}

	public ChangePhoneNumberRequestHandler() {
		super(ChangePhoneNumberRequest::new);
	}
}
