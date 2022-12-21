package pala.apps.arlith.backend.networking.scp;

import java.time.Instant;

import pala.libs.generic.events.Event;
import pala.libs.generic.events.EventType;

public class ClientEvent extends Event {
	public static EventType<ClientEvent> CLIENT_EVENT = new EventType<>(EventType.EVENT);

	private final CommunicationConnection client;

	public ClientEvent(Instant timestamp, CommunicationConnection client) {
		super(timestamp);
		this.client = client;
	}

	public ClientEvent(CommunicationConnection client) {
		this.client = client;
	}

	public CommunicationConnection getClient() {
		return client;
	}

}
