package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.errors.ServerError;
import pala.apps.arlith.backend.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.UserValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetUserRequest extends SimpleCommunicationProtocolRequest<UserValue> {

	public static final String REQUEST_NAME = "get-user";
	private final static String USER_KEY = "user";

	public GIDValue getUser() {
		return user;
	}

	public GetUserRequest(GIDValue user) {
		super(REQUEST_NAME);
		setUser(user);
	}

	public GetUserRequest(JSONObject json) {
		super(REQUEST_NAME, json);
		user = new GIDValue(json.get(USER_KEY));
	}

	public void setUser(GIDValue user) {
		this.user = user;
	}

	private GIDValue user;

	@Override
	protected void build(JSONObject object) {
		object.put(USER_KEY, user.json());
	}

	@Override
	protected UserValue parseReturnValue(JSONValue json) {
		return new UserValue(json);
	}

	@Override
	public UserValue receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, AccessDeniedError, ObjectNotFoundError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | AccessDeniedError
				| ObjectNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
