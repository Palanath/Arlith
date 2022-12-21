package pala.apps.arlith.backend.common.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public class UserJoinedCommunityEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "user-joined-community";
	private static final String COMMUNITY_KEY = "community", USER_KEY = "user";
	public static final EventType<UserJoinedCommunityEvent> USER_JOINED_COMMUNITY_EVENT = new EventType<>(
			COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue community, user;

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
		object.put(USER_KEY, user.json());
	}

	public UserJoinedCommunityEvent(Instant timestamp, GIDValue community, GIDValue user) {
		super(EVENT_NAME, timestamp);
		this.community = community;
		this.user = user;
	}

	public UserJoinedCommunityEvent(GIDValue community, GIDValue user) {
		super(EVENT_NAME);
		this.community = community;
		this.user = user;
	}

	public UserJoinedCommunityEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
		user = new GIDValue(properties.get(USER_KEY));
	}

	public GIDValue getCommunity() {
		return community;
	}

	public GIDValue getUser() {
		return user;
	}

}
