package pala.apps.arlith.api.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class TFARequiredError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "tfa-required";

	public TFARequiredError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public TFARequiredError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {

	}

}
