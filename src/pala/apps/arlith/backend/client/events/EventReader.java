package pala.apps.arlith.backend.client.events;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

public interface EventReader {
	public EventInstance<?> apply(Connection c) throws IllegalCommunicationProtocolException, ClassCastException,
			IllegalArgumentException, CommunicationProtocolError, UnknownCommStateException, BlockException;
}
