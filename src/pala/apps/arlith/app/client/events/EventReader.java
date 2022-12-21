package pala.apps.arlith.app.client.events;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;

public interface EventReader {
	public EventInstance<?> apply(CommunicationConnection c)
			throws IllegalCommunicationProtocolException, ClassCastException, IllegalArgumentException, CommunicationProtocolError;
}
