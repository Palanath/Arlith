package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.UserValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ListFriendsRequest extends SimpleCommunicationProtocolRequest<ListValue<UserValue>> {

	public static final String REQUEST_NAME = "list-friends";

	public ListFriendsRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
	}

	public ListFriendsRequest() {
		super(REQUEST_NAME);
	}

	@Override
	protected void build(JSONObject object) {
	}

	@Override
	protected ListValue<UserValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, UserValue::new);
	}

	@Override
	public ListValue<UserValue> receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
