package pala.apps.arlith.libraries.networking.scp;

import java.net.Socket;
import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerSocketAcceptFailureEvent extends ServerExceptionEvent {

	public static final EventType<ServerSocketAcceptFailureEvent> SERVER_SOCKET_ACCEPT_FAILURE_EVENT = new EventType<>(
			SERVER_EXCEPTION_EVENT);

	private final Socket socket;

	public Socket getSocket() {
		return socket;
	}

	public ServerSocketAcceptFailureEvent(Instant timestamp, CommunicationConnectionAcceptor server, Exception exception,
			Socket socket) {
		super(timestamp, server, exception);
		this.socket = socket;
	}

	public ServerSocketAcceptFailureEvent(CommunicationConnectionAcceptor server, Exception exception, Socket socket) {
		super(server, exception);
		this.socket = socket;
	}

}
