package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.IntegerValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class FriendByGIDRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public GIDValue getId() {
		return id;
	}

	public void setId(GIDValue id) {
		this.id = id;
	}

	public static final String REQUEST_NAME = "friend-by-gid";
	private final static String ID_KEY = "id";

	private GIDValue id;

	public FriendByGIDRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		if (properties.containsKey(ID_KEY))
			id = new GIDValue(properties.get(ID_KEY));
		else
			throw new CommunicationProtocolConstructionError("friend-request does not contain GID.", properties);
	}

	public FriendByGIDRequest(GIDValue gid) {
		super(REQUEST_NAME);
		id = gid;
	}

	public FriendByGIDRequest(TextValue username, IntegerValue disc) {
		super(REQUEST_NAME);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(ID_KEY, id.json());
	}

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	@Override
	public CompletionValue receiveResponse(Connection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, ObjectNotFoundError, IllegalCommunicationProtocolException,
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
