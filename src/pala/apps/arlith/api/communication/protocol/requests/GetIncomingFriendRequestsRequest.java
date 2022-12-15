package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetIncomingFriendRequestsRequest extends SimpleCommunicationProtocolRequest<ListValue<GIDValue>> {

	public static final String REQUEST_NAME = "get-incoming-friend-requests";

	public GetIncomingFriendRequestsRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
	}

	public GetIncomingFriendRequestsRequest() {
		super(REQUEST_NAME);
	}

	@Override
	protected void build(JSONObject object) {
	}

	@Override
	public ListValue<GIDValue> receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected ListValue<GIDValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, GIDValue::new);
	}

}
