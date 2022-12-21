package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetPhoneNumberRequest;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public final class GetPhoneNumberRequestHandler extends SimpleRequestHandler<GetPhoneNumberRequest> {

	@Override
	protected void handle(GetPhoneNumberRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else
			client.sendResult(new TextValue(client.getUser().getPhoneNumber()));
	}

	public GetPhoneNumberRequestHandler() {
		super(GetPhoneNumberRequest::new);
	}
}
