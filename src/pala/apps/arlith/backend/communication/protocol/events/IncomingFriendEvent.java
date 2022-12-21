package pala.apps.arlith.backend.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.types.FriendStateValue;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Signifies that a friend request was sent from a user to the currently logged
 * in user. This event contains the {@link #getUser() user} who sent the
 * request, the {@link #getPreviousState() previous friend state} that this user
 * had with the sending user, and the {@link #getNewState() new friend state}
 * that this user now has with the sending user.
 * </p>
 * <p>
 * This event is sent to the recipient of a friend request.
 * </p>
 * 
 * @author Palanath
 *
 */
public class IncomingFriendEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "incoming-friend";
	private static final String USER_KEY = "user", PREVIOUS_STATE_KEY = "previous-state", NEW_STATE_KEY = "new-state",
			NOTIFICATION_KEY = "notif";
	public static final EventType<IncomingFriendEvent> INCOMING_FRIEND_EVENT = new EventType<>(COMMUNICATION_PROTOCOL_EVENT);
	private final GIDValue user, notificationID;
	private final FriendStateValue previousState, newState;

	public GIDValue getNotificationID() {
		return notificationID;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
		object.put(PREVIOUS_STATE_KEY, previousState.json());
		object.put(NEW_STATE_KEY, newState.json());
		object.put(NOTIFICATION_KEY, notificationID.json());
	}

	public IncomingFriendEvent(GIDValue friend, FriendStateValue previousState, FriendStateValue newState,
			GIDValue notificiationID, Instant timestamp) {
		super(EVENT_NAME, timestamp);
		user = friend;
		this.previousState = previousState;
		this.newState = newState;
		this.notificationID = notificiationID;
	}

	public IncomingFriendEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
		previousState = FriendStateValue.fromJSON(properties.get(PREVIOUS_STATE_KEY));
		newState = FriendStateValue.fromJSON(properties.get(NEW_STATE_KEY));
		notificationID = new GIDValue(properties.get(NOTIFICATION_KEY));
	}

	public IncomingFriendEvent(GIDValue friend, FriendStateValue previousState, FriendStateValue newState,
			GIDValue notificationID) {
		super(EVENT_NAME);
		user = friend;
		this.previousState = previousState;
		this.newState = newState;
		this.notificationID = notificationID;
	}

	public GIDValue getUser() {
		return user;
	}

	public FriendStateValue getPreviousState() {
		return previousState;
	}

	public FriendStateValue getNewState() {
		return newState;
	}

}
