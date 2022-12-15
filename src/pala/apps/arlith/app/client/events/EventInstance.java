package pala.apps.arlith.app.client.events;

import pala.apps.arlith.api.communication.protocol.events.CommunicationProtocolEvent;
import pala.libs.generic.events.EventType;

public class EventInstance<E extends CommunicationProtocolEvent> {
	public EventType<E> getType() {
		return type;
	}

	public E getEvent() {
		return event;
	}

	private final EventType<E> type;
	private final E event;

	public EventInstance(EventType<E> type, E event) {
		this.type = type;
		this.event = event;
	}

}
