package pala.apps.arlith.api.connections.scp;

import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerExceptionEvent extends ServerEvent {

	public static EventType<ServerExceptionEvent> SERVER_EXCEPTION_EVENT = new EventType<>(SERVER_EVENT);

	private final Exception exception;

	public Exception getException() {
		return exception;
	}

	public ServerExceptionEvent(Instant timestamp, CommunicationConnectionAcceptor server, Exception exception) {
		super(timestamp, server);
		this.exception = exception;
	}

	public ServerExceptionEvent(CommunicationConnectionAcceptor server, Exception exception) {
		super(server);
		this.exception = exception;
	}

}
