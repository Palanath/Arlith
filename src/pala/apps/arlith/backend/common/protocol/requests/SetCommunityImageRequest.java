package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.Connection;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class SetCommunityImageRequest extends CommunicationProtocolRequest<CompletionValue> {
	public static final String REQUEST_NAME = "set-community-image";

	private static final String COMMUNITY_KEY = "community", TYPE_KEY = "type", IMAGE_KEY = "image";

	private GIDValue community;
	private TextValue type;
	private PieceOMediaValue image;

	public SetCommunityImageRequest(GIDValue community, TextValue type, PieceOMediaValue image) {
		super(REQUEST_NAME);
		this.community = community;
		this.type = type;
		this.image = image;
	}

	public SetCommunityImageRequest(JSONObject properties, Connection connection)
			throws UnknownCommStateException, BlockException {
		super(REQUEST_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
		type = new TextValue(properties.get(TYPE_KEY));
		image = properties.containsKey(IMAGE_KEY) ? new PieceOMediaValue(properties.get(IMAGE_KEY), connection) : null;
	}

	public GIDValue getCommunity() {
		return community;
	}

	public SetCommunityImageRequest setCommunity(GIDValue community) {
		this.community = community;
		return this;
	}

	public TextValue getType() {
		return type;
	}

	public SetCommunityImageRequest setType(TextValue type) {
		this.type = type;
		return this;
	}

	public PieceOMediaValue getImage() {
		return image;
	}

	public SetCommunityImageRequest setImage(PieceOMediaValue image) {
		this.image = image;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
		object.put(TYPE_KEY, type.json());
		if (image != null)
			object.put(IMAGE_KEY, image.json());
	}

	@Override
	protected void sendAuxiliaryData(CommunicationConnection connection) {
		if (image != null)
			image.sendAuxiliaryData(connection);
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected CompletionValue parseReturnValue(JSONValue json, CommunicationConnection connection) {
		return new CompletionValue(json);
	}

}