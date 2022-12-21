package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.MediaNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.errors.ServerError;
import pala.apps.arlith.backend.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetProfileIconRequest extends CommunicationProtocolRequest<PieceOMediaValue> {

	public static final String REQUEST_NAME = "get-profile-icon";
	private static final String USER_KEY = "user";
	private GIDValue user;

	public GIDValue getUser() {
		return user;
	}

	public void setUser(GIDValue user) {
		this.user = user;
	}

	public GetProfileIconRequest(GIDValue user) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.user = user;
	}

	public GetProfileIconRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
	}

	@Override
	public PieceOMediaValue receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, MediaNotFoundError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | MediaNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected PieceOMediaValue parseReturnValue(JSONValue json, CommunicationConnection connection) {
		return PieceOMediaValue.fromNullable(json, connection);
	}

}
