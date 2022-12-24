package pala.apps.arlith.backend.client;

import pala.apps.arlith.backend.client.events.EventSubsystem;
import pala.apps.arlith.backend.client.events.StandardEventReader;
import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.common.protocol.types.BooleanValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

class StandardEventSubsystem extends EventSubsystem {
	private final AuthToken token;

	public StandardEventSubsystem(CommunicationConnection client, AuthToken token) {
		super(client, new StandardEventReader());
		this.token = token;
	}

	@Override
	protected void authorize(CommunicationConnection connection) throws CommunicationProtocolError {
		AuthRequest req = new AuthRequest(token);
		req.setEventConnection(BooleanValue.TRUE);
		req.sendRequest(connection);
		req.receiveResponse(connection);
	}

}