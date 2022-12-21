package pala.apps.arlith.backend.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Fired upon the changing of a user's profile icon. This event signifies that a
 * user changed its profile icon. This event does not contain the new profile
 * icon; the client must query it from the server manually, upon receipt of this
 * event, to determine what the icon changed to.
 * </p>
 * <p>
 * This event is sent sparingly, so it is not sent to the client of the user
 * that changed its profile icon.
 * </p>
 * 
 * @author Palanath
 *
 */
public class LazyProfileIconChangedEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "lazy-profile-icon-changed";
	private static final String USER_KEY = "user";

	public static final EventType<LazyProfileIconChangedEvent> LAZY_PROFILE_ICON_CHANGED_EVENT = new EventType<>(
			COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue user;

	public GIDValue getUser() {
		return user;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
	}

	public LazyProfileIconChangedEvent(Instant timestamp, GIDValue user) {
		super(EVENT_NAME, timestamp);
		this.user = user;
	}

	public LazyProfileIconChangedEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
	}

	public LazyProfileIconChangedEvent(GIDValue user) {
		super(EVENT_NAME);
		this.user = user;
	}

}
