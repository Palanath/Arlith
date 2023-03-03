package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetStatusRequest extends SimpleCommunicationProtocolRequest<TextValue> {

	public final static String REQUEST_NAME = "get-status";
	private final static String USER_KEY = "user";

	private GIDValue user;

	public GIDValue getUser() {
		return user;
	}

	public void setUser(GIDValue user) {
		this.user = user;
	}

	public GetStatusRequest(GIDValue user) {
		super(REQUEST_NAME);
		this.user = user;
	}

	public GetStatusRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		user = new GIDValue(properties.get(USER_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
	}

	@Override
	protected TextValue parseReturnValue(JSONValue json) {
		return json == JSONConstant.NULL ? null : new TextValue(json);
	}

	@Override
	public TextValue receiveResponse(Connection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, ObjectNotFoundError,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
