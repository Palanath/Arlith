package pala.apps.arlith.api.connections.scp;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerSocketCloseFailureEvent extends ServerExceptionEvent {

	public static final EventType<ServerSocketCloseFailureEvent> SERVER_SOCKET_CLOSE_FAILURE_EVENT = new EventType<>(
			SERVER_EXCEPTION_EVENT);

	private final ServerSocket socket;

	public ServerSocket getSocket() {
		return socket;
	}

	public ServerSocketCloseFailureEvent(Instant timestamp, CommunicationConnectionAcceptor server, IOException exception,
			ServerSocket socket) {
		super(timestamp, server, exception);
		this.socket = socket;
	}

	public ServerSocketCloseFailureEvent(CommunicationConnectionAcceptor server, IOException exception,
			ServerSocket socket) {
		super(server, exception);
		this.socket = socket;
	}

}
