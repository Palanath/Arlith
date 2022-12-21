package pala.apps.arlith.backend.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Fired upon the changing of one of a community's images. This event informs
 * the client that a community's image has changed, and provides information
 * regarding which community image changed (background image, icon, etc). The
 * new image must be queried manually, since this event does not include the
 * actual image, (as images may be large and the client may not actually need to
 * update the image yet).
 * </p>
 * <p>
 * This event is sent sparingly, meaning that it is only sent out to clients
 * that would not otherwise have the information it contains. Such sparsity
 * manifests through lack of this event being sent to the client which changes
 * the community's image, or otherwise knowingly causes the event to fire.
 * </p>
 * <p>
 * This event is sent to all members of the community whose image changed,
 * except for the one who changed its image, if any.
 * </p>
 * 
 * @author Palanath
 *
 */
public class LazyCommunityImageChangedEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "lazy-community-image-changed";
	private static final String COMMUNITY_KEY = "community", IMAGE_TYPE = "type";

	public static final EventType<LazyCommunityImageChangedEvent> LAZY_COMMUNITY_IMAGE_CHANGED_EVENT = new EventType<>(
			COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue community;
	private final TextValue type;

	public GIDValue getCommunity() {
		return community;
	}

	public TextValue getType() {
		return type;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
		object.put(IMAGE_TYPE, type.json());
	}

	public LazyCommunityImageChangedEvent(Instant timestamp, GIDValue community, TextValue type) {
		super(EVENT_NAME, timestamp);
		this.community = community;
		this.type = type;
	}

	public LazyCommunityImageChangedEvent(GIDValue community, TextValue type) {
		super(EVENT_NAME);
		this.community = community;
		this.type = type;
	}

	public LazyCommunityImageChangedEvent(JSONObject props) {
		super(EVENT_NAME, props);
		community = new GIDValue(props.get(COMMUNITY_KEY));
		type = new TextValue(props.get(IMAGE_TYPE));
	}

}
