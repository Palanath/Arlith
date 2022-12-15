package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.authentication.AuthToken;
import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.AuthError;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.AuthTokenValue;
import pala.apps.arlith.api.communication.protocol.types.BooleanValue;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class AuthRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public static final String REQUEST_NAME = "auth";
	private final static String AUTH_TOKEN_KEY = "auth-token", EVENT_CONNECTION_KEY = "event-connection";

	private AuthTokenValue authToken;
	private BooleanValue eventConnection = new BooleanValue(false);

	public BooleanValue getEventConnection() {
		return eventConnection;
	}

	public void setEventConnection(BooleanValue eventConnection) {
		this.eventConnection = eventConnection;
	}

	public AuthRequest(JSONObject json) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, json);
		authToken = new AuthTokenValue(json.get(AUTH_TOKEN_KEY));
		eventConnection = new BooleanValue(json.get(EVENT_CONNECTION_KEY));
	}

	public AuthRequest(AuthToken token) {
		this(new AuthTokenValue(token));
	}

	public AuthRequest(AuthTokenValue token) {
		super(REQUEST_NAME);
		setAuthToken(token);
	}

	public void setAuthToken(AuthTokenValue authToken) {
		this.authToken = authToken;
	}

	public AuthTokenValue getAuthToken() {
		return authToken;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(AUTH_TOKEN_KEY, authToken.json());
		object.put(EVENT_CONNECTION_KEY, eventConnection.json());
	}

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
			throws AuthError, SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
