package pala.apps.arlith.backend.networking.scp;

import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerThreadSystemRestartingEvent extends ServerEvent {

	public static EventType<ServerThreadSystemRestartingEvent> SERVER_THREAD_SYSTEM_RESTARTING_EVENT = new EventType<>(
			SERVER_EVENT);

	public ServerThreadSystemRestartingEvent(Instant timestamp, CommunicationConnectionAcceptor server) {
		super(timestamp, server);
	}

	public ServerThreadSystemRestartingEvent(CommunicationConnectionAcceptor server) {
		super(server);
	}

}
