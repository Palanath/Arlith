package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.IntegerValue;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public @Deprecated class GetSimultaneousMediaUploadsLimitRequest extends SimpleCommunicationProtocolRequest<IntegerValue> {

	private final static String REQUEST_NAME = "get-simultaneous-media-uploads-limit";

	public GetSimultaneousMediaUploadsLimitRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
	}

	public GetSimultaneousMediaUploadsLimitRequest() {
		super(REQUEST_NAME);
	}

	@Override
	protected void build(JSONObject object) {
	}

	@Override
	protected IntegerValue parseReturnValue(JSONValue json) {
		return new IntegerValue(json);
	}

}
