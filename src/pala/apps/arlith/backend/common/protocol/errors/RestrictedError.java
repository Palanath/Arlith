package pala.apps.arlith.backend.common.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class RestrictedError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "restricted";

	public RestrictedError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public RestrictedError(String message) {
		super(ERROR_TYPE, message);
	}

	public RestrictedError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
