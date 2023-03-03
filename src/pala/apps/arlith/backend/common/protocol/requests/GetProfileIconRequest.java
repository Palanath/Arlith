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
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
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
	public PieceOMediaValue receiveResponse(Connection client) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, MediaNotFoundError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | MediaNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected PieceOMediaValue parseReturnValue(JSONValue json, Connection connection)
			throws UnknownCommStateException, BlockException {
		return PieceOMediaValue.fromNullable(json, connection);
	}

}
