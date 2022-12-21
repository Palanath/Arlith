package pala.apps.arlith.backend.connections.scp;

import java.time.Instant;

import pala.libs.generic.events.EventType;

public class ServerThreadSystemRestartFailureEvent extends ServerExceptionEvent {

	public static EventType<ServerThreadSystemRestartFailureEvent> SERVER_THREAD_SYSTEM_RESTART_FAILURE_EVENT = new EventType<>(
			SERVER_EXCEPTION_EVENT);

	public ServerThreadSystemRestartFailureEvent(Instant timestamp, CommunicationConnectionAcceptor server,
			Exception exception) {
		super(timestamp, server, exception);
	}

	public ServerThreadSystemRestartFailureEvent(CommunicationConnectionAcceptor server, Exception exception) {
		super(server, exception);
	}

}
