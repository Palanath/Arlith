package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.ChangeUsernameRequest;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class ChangeUsernameRequestHandler extends SimpleRequestHandler<ChangeUsernameRequest> {

	@Override
	protected void handle(ChangeUsernameRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else
			client.sendResult(new TextValue(client.getUser().changeUsername(r.getName().getValue())));
	}

	public ChangeUsernameRequestHandler() {
		super(ChangeUsernameRequest::new);
	}
}
