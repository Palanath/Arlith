package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetEmailRequest extends SimpleCommunicationProtocolRequest<TextValue> {

	public static final String REQUEST_NAME = "get-email";

	@Override
	protected TextValue parseReturnValue(JSONValue json) {
		return new TextValue(json);
	}

	@Override
	protected void build(JSONObject object) {
	}

	public GetEmailRequest() {
		super(REQUEST_NAME);
	}

	public GetEmailRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
	}

}
