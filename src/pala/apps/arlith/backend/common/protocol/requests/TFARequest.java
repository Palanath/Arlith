package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.InvalidConnectionStateError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.errors.TFAError;
import pala.apps.arlith.backend.common.protocol.errors.TFARequiredError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class TFARequest extends SimpleCommunicationProtocolRequest<AuthTokenValue> {
	public HexHashValue getTfaCode() {
		return tfaCode;
	}

	public void setTfaCode(HexHashValue tfaCode) {
		this.tfaCode = tfaCode;
	}

	private static final String REQUEST_NAME = "tfa", TFA_CODE_KEY = "tfa-code";

	private HexHashValue tfaCode;

	public TFARequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		tfaCode = new HexHashValue(properties.get(TFA_CODE_KEY));
	}

	public TFARequest(HexHashValue hash) {
		super(REQUEST_NAME);
		setTfaCode(hash);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(TFA_CODE_KEY, tfaCode.json());
	}

	@Override
	protected AuthTokenValue parseReturnValue(JSONValue json) {
		return new AuthTokenValue(json);
	}

	@Override
	public AuthTokenValue receiveResponse(Connection client)
			throws TFAError, TFARequiredError, SyntaxError, RateLimitError, ServerError,
			InvalidConnectionStateError, RestrictedError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		// TODO Auto-generated method stub
		try {
			return super.receiveResponse(client);
		} catch (TFAError | TFARequiredError | SyntaxError | RateLimitError | ServerError
				| InvalidConnectionStateError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
