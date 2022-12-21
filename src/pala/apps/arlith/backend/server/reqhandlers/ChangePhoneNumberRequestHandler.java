package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;

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
