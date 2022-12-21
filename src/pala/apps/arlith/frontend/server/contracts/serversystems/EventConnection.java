package pala.apps.arlith.frontend.server.contracts.serversystems;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;

public interface EventConnection extends ClientConnection {
	/**
	 * Sends the specified {@link CommunicationProtocolEvent} over this connection to the connected
	 * client.
	 * 
	 * @param event The {@link CommunicationProtocolEvent} to send.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs, causing the connection to be
	 *                                   considered dead.
	 */
	default void sendEvent(CommunicationProtocolEvent event) throws UnknownCommStateException {
		event.send(getConnection());
	}

	/**
	 * Returns the {@link GID} of the user connected. This is always established
	 * upon construction for {@link EventConnection}s.
	 */
	@Override
	GID getUserID();
}
