package pala.apps.arlith.backend.common.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public class ProfileIconChangedEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "profile-icon-changed";
	private static final String USER_KEY = "user";

	public static final EventType<ProfileIconChangedEvent> PROFILE_ICON_CHANGED_EVENT = new EventType<>(COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue user;
	private final byte[] oldIcon, newIcon;

	public GIDValue getUser() {
		return user;
	}

	public byte[] getOldIcon() {
		return oldIcon;
	}

	public byte[] getNewIcon() {
		return newIcon;
	}

	@Override
	public void send(Connection comm) throws UnknownCommStateException {
		super.send(comm);
		comm.writeBlock(oldIcon);
		comm.writeBlock(newIcon);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
	}

	public ProfileIconChangedEvent(Instant timestamp, GIDValue user, byte[] oldIcon, byte[] newIcon) {
		super(EVENT_NAME, timestamp);
		this.user = user;
		this.oldIcon = oldIcon;
		this.newIcon = newIcon;
	}

	public ProfileIconChangedEvent(JSONObject properties, byte[] oldIcon, byte[] newIcon) {
		super(EVENT_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
		this.oldIcon = oldIcon;
		this.newIcon = newIcon;
	}

	public ProfileIconChangedEvent(GIDValue user, byte[] oldIcon, byte[] newIcon) {
		super(EVENT_NAME);
		this.user = user;
		this.oldIcon = oldIcon;
		this.newIcon = newIcon;
	}

}
