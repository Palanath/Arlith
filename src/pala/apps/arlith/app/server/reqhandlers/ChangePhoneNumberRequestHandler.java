package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;

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
