package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ListPublicCommunitiesRequest extends SimpleCommunicationProtocolRequest<ListValue<GIDValue>> {
	public static final String REQUEST_NAME = "list-public-communities";

	public ListPublicCommunitiesRequest() {
		super(REQUEST_NAME);
	}

	public ListPublicCommunitiesRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
	}

	@Override
	protected void build(JSONObject object) {
	}

	@Override
	public ListValue<GIDValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, GIDValue::new);
	}

	@Override
	public ListValue<GIDValue> receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}