package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class FriendByNameRequest extends SimpleCommunicationProtocolRequest<GIDValue> {
	public static final String REQUEST_NAME = "friend-by-name";

	private static final String USERNAME_KEY = "username", DISC_KEY = "disc";

	private TextValue username;
	private TextValue disc;

	public FriendByNameRequest(TextValue username, TextValue disc) {
		super(REQUEST_NAME);
		this.username = username;
		this.disc = disc;
	}

	public FriendByNameRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		username = new TextValue(properties.get(USERNAME_KEY));
		disc = new TextValue(properties.get(DISC_KEY));
	}

	public TextValue getUsername() {
		return username;
	}

	public FriendByNameRequest setUsername(TextValue username) {
		this.username = username;
		return this;
	}

	public TextValue getDisc() {
		return disc;
	}

	public FriendByNameRequest setDisc(TextValue disc) {
		this.disc = disc;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USERNAME_KEY, username.json());
		object.put(DISC_KEY, disc.json());
	}

	@Override
	public GIDValue parseReturnValue(JSONValue json) {
		return new GIDValue(json);
	}

	@Override
	public GIDValue receiveResponse(CommunicationConnection client) throws IllegalCommunicationProtocolException, SyntaxError,
			RateLimitError, ServerError, ObjectNotFoundError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | ObjectNotFoundError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}