package pala.apps.arlith.backend.client.events;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.events.ProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.StatusChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.ThreadAccessGainedEvent;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public class StandardEventReader implements EventReader {

	@Override
	public EventInstance<?> apply(CommunicationConnection c)
			throws IllegalCommunicationProtocolException, ClassCastException, IllegalArgumentException, CommunicationProtocolError {
		JSONValue o = c.readJSON();
		if (!(o instanceof JSONObject))
			throw new CommunicationProtocolConstructionError("Server provided an invalid type of JSONValue for an event.");
		JSONObject t = (JSONObject) o;

		if (!(t.get("event") instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Malformed event received from server.", t);

		switch (t.getString("event")) {
		// XXX Add events
		case MessageCreatedEvent.EVENT_NAME:
			return new EventInstance<>(MessageCreatedEvent.MESSAGE_CREATED_EVENT, new MessageCreatedEvent(t));
		case IncomingFriendEvent.EVENT_NAME:
			return new EventInstance<>(IncomingFriendEvent.INCOMING_FRIEND_EVENT, new IncomingFriendEvent(t));
		case ThreadAccessGainedEvent.EVENT_NAME:
			return new EventInstance<>(ThreadAccessGainedEvent.THREAD_ACCESS_GAINED_EVENT,
					new ThreadAccessGainedEvent(t));
		case StatusChangedEvent.EVENT_NAME:
			return new EventInstance<>(StatusChangedEvent.STATUS_CHANGED_EVENT, new StatusChangedEvent(t));
		case ProfileIconChangedEvent.EVENT_NAME:
			return new EventInstance<>(ProfileIconChangedEvent.PROFILE_ICON_CHANGED_EVENT,
					new ProfileIconChangedEvent(t, c.readBlockLong(), c.readBlockLong()));
		case LazyProfileIconChangedEvent.EVENT_NAME:
			return new EventInstance<>(LazyProfileIconChangedEvent.LAZY_PROFILE_ICON_CHANGED_EVENT,
					new LazyProfileIconChangedEvent(t));
		case LazyCommunityImageChangedEvent.EVENT_NAME:
			return new EventInstance<>(LazyCommunityImageChangedEvent.LAZY_COMMUNITY_IMAGE_CHANGED_EVENT,
					new LazyCommunityImageChangedEvent(t));
//		case CommunityCreatedEvent.EVENT_NAME:
//			return new EventInstance<>(CommunityCreatedEvent.COMMUNITY_CREATED_EVENT,
//					new CommunityCreatedEvent(t));

		default:
			throw new CommunicationProtocolConstructionError("Unknown event received from server: " + t.getString("event"), t);
		}
	}

}
