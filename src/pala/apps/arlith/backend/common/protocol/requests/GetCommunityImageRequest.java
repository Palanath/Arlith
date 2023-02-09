package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.MediaNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetCommunityImageRequest extends CommunicationProtocolRequest<PieceOMediaValue> {

	public static final String REQUEST_NAME = "get-community-image";
	private static final String COMMUNITY_KEY = "community", TYPE_KEY = "type";
	private GIDValue community;
	/**
	 * The type of the community image being retrieved.
	 */
	private TextValue type;

	public TextValue getType() {
		return type;
	}

	public void setType(TextValue type) {
		this.type = type;
	}

	public GIDValue getCommunity() {
		return community;
	}

	public void setCommunity(GIDValue community) {
		this.community = community;
	}

	public GetCommunityImageRequest(GIDValue community, TextValue type) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.community = community;
		this.type = type;
	}

	public GetCommunityImageRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
		type = new TextValue(properties.get(TYPE_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
		object.put(TYPE_KEY, type.json());
	}

	@Override
	protected PieceOMediaValue parseReturnValue(JSONValue json, Connection connection)
			throws UnknownCommStateException, BlockException {
		return PieceOMediaValue.fromNullable(json, connection);
	}

	@Override
	public PieceOMediaValue receiveResponse(Connection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, MediaNotFoundError,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | MediaNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
