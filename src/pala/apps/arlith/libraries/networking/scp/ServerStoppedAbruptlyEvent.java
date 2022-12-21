package pala.apps.arlith.libraries.networking.scp;

import java.io.IOException;
import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerStoppedAbruptlyEvent extends ServerExceptionEvent {

	public static EventType<ServerStoppedAbruptlyEvent> SERVER_STOPPED_ABRUPTLY_EVENT = new EventType<>(
			SERVER_EXCEPTION_EVENT);

	public ServerStoppedAbruptlyEvent(Instant timestamp, CommunicationConnectionAcceptor server, IOException exception) {
		super(timestamp, server, exception);
	}

	public ServerStoppedAbruptlyEvent(CommunicationConnectionAcceptor server, IOException exception) {
		super(server, exception);
	}

}
