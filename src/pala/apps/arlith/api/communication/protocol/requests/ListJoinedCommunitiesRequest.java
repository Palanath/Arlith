package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.CommunityValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ListJoinedCommunitiesRequest extends SimpleCommunicationProtocolRequest<ListValue<CommunityValue>> {

	public static final String REQUEST_NAME = "list-joined-communities";

	@Override
	protected ListValue<CommunityValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, CommunityValue::new);
	}

	@Override
	public ListValue<CommunityValue> receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, AccessDeniedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | AccessDeniedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected void build(JSONObject object) {
	}

	public ListJoinedCommunitiesRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
	}

	public ListJoinedCommunitiesRequest() {
		super(REQUEST_NAME);
	}

}
