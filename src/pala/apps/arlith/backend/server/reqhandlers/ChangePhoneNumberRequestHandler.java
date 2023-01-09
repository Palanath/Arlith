package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ChangePhoneNumberRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.CreateAccountProblemValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class ChangePhoneNumberRequestHandler extends SimpleRequestHandler<ChangePhoneNumberRequest> {

	@Override
	protected void handle(ChangePhoneNumberRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			if (r.getPhoneNumber() != null)
				if (Utilities.checkPhoneNumberValidity(r.phoneNumber()) != null) {
					client.sendError(new CreateAccountError(CreateAccountProblemValue.ILLEGAL_PH));
					return;
				} else if (client.getWorld().checkIfPhoneTaken(r.phoneNumber())) {
					client.sendError(new CreateAccountError(CreateAccountProblemValue.TAKEN_PH));
					return;
				}
			client.getUser().changePhone(r.phoneNumber());
			client.sendResult(new CompletionValue());
		}
	}

	public ChangePhoneNumberRequestHandler() {
		super(ChangePhoneNumberRequest::new);
	}
}
