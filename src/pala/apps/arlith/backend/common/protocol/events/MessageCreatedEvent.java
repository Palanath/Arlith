package pala.apps.arlith.backend.common.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public class MessageCreatedEvent extends MessageEvent {

	public static final String EVENT_NAME = "message-created", NOTIFICATION_KEY = "notif";
	public static final EventType<MessageCreatedEvent> MESSAGE_CREATED_EVENT = new EventType<>(
			MESSAGE_EVENT);

	private final GIDValue notificationID;

	public GIDValue getNotificationID() {
		return notificationID;
	}

	public MessageCreatedEvent(Instant timestamp, MessageValue message, GIDValue notificationID) {
		super(EVENT_NAME, timestamp, message);
		this.notificationID = notificationID;
	}

	public MessageCreatedEvent(MessageValue message, GIDValue notificationID) {
		super(EVENT_NAME, message);
		this.notificationID = notificationID;
	}

	public MessageCreatedEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		notificationID = new GIDValue(properties.get(NOTIFICATION_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		super.build(object);
		object.put(NOTIFICATION_KEY, notificationID.json());
	}

}
