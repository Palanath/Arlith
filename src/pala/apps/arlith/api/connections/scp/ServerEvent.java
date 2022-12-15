package pala.apps.arlith.api.connections.scp;

import java.time.Instant;

import pala.libs.generic.events.Event;
import pala.libs.generic.events.EventType;

public class ServerEvent extends Event {
	public static final EventType<ServerEvent> SERVER_EVENT = new EventType<>(EventType.EVENT);
	private final CommunicationConnectionAcceptor server;

	public ServerEvent(Instant timestamp, CommunicationConnectionAcceptor server) {
		super(timestamp);
		this.server = server;
	}

	public ServerEvent(CommunicationConnectionAcceptor server) {
		this.server = server;
	}

	public CommunicationConnectionAcceptor getServer() {
		return server;
	}

}
