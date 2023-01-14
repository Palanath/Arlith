package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError.CreateAccountProblemValue;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ChangeUsernameRequest;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class ChangeUsernameRequestHandler extends SimpleRequestHandler<ChangeUsernameRequest> {

	@Override
	protected void handle(ChangeUsernameRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else // Check to make sure username valid.
		if (Utilities.checkUsernameValidity(r.getName().getValue()) != null)
			client.sendError(new CreateAccountError(CreateAccountProblemValue.ILLEGAL_UN));
		else
			client.sendResult(new TextValue(client.getUser().changeUsername(r.getName().getValue())));
	}

	public ChangeUsernameRequestHandler() {
		super(ChangeUsernameRequest::new);
	}
}
