package pala.apps.arlith.api.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.api.communication.protocol.types.MessageValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public abstract class MessageEvent extends CommunicationProtocolEvent {
	public MessageValue getMessage() {
		return message;
	}

	private static final String MESSAGE_KEY = "message";
	public static final EventType<MessageEvent> MESSAGE_EVENT = new EventType<>(COMMUNICATION_PROTOCOL_EVENT);

	private final MessageValue message;

	public MessageEvent(String name, Instant timestamp, MessageValue message) {
		super(name, timestamp);
		this.message = message;
	}

	public MessageEvent(String name, MessageValue message) {
		super(name);
		this.message = message;
	}

	public MessageEvent(String requiredName, JSONObject properties) {
		super(requiredName, properties);
		this.message = new MessageValue(properties.get(MESSAGE_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(MESSAGE_KEY, message.json());
	}

}
