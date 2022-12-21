package pala.apps.arlith.backend.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public class StatusChangedEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "status-changed";
	private static final String USER_KEY = "user", OLD_STATUS_KEY = "old-status", NEW_STATUS_KEY = "new-status";

	public static final EventType<StatusChangedEvent> STATUS_CHANGED_EVENT = new EventType<>(COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue user;
	private final TextValue oldStatus, newStatus;

	public GIDValue getUser() {
		return user;
	}

	public TextValue getOldStatus() {
		return oldStatus;
	}

	public TextValue getNewStatus() {
		return newStatus;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
		object.put(OLD_STATUS_KEY, oldStatus.json());
		object.put(NEW_STATUS_KEY, newStatus.json());
	}

	public StatusChangedEvent(Instant timestamp, GIDValue user, TextValue oldStatus, TextValue newStatus) {
		super(EVENT_NAME, timestamp);
		this.user = user;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

	public StatusChangedEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
		oldStatus = new TextValue(properties.get(OLD_STATUS_KEY));
		newStatus = new TextValue(properties.get(NEW_STATUS_KEY));
	}

	public StatusChangedEvent(GIDValue user, TextValue oldStatus, TextValue newStatus) {
		super(EVENT_NAME);
		this.user = user;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
	}

}
