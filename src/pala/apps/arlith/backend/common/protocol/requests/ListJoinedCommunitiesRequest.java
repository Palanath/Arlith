package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ListJoinedCommunitiesRequest extends SimpleCommunicationProtocolRequest<ListValue<CommunityValue>> {

	public static final String REQUEST_NAME = "list-joined-communities";

	@Override
	protected ListValue<CommunityValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, CommunityValue::new);
	}

	@Override
	public ListValue<CommunityValue> receiveResponse(Connection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
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
