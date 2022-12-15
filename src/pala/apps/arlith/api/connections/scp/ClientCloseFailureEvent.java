package pala.apps.arlith.api.connections.scp;

import java.io.IOException;
import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ClientCloseFailureEvent extends ClientEvent {

	public static final EventType<ClientCloseFailureEvent> CLIENT_CLOSE_FAILURE_EVENT = new EventType<>(CLIENT_EVENT);

	private final IOException exception;

	public IOException getException() {
		return exception;
	}

	public ClientCloseFailureEvent(Instant timestamp, CommunicationConnection client, IOException exception) {
		super(timestamp, client);
		this.exception = exception;
	}

	public ClientCloseFailureEvent(CommunicationConnection client, IOException exception) {
		super(client);
		this.exception = exception;
	}

}
