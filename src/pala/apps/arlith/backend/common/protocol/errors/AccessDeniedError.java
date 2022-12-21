package pala.apps.arlith.backend.common.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class AccessDeniedError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "access-denied";

	public AccessDeniedError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public AccessDeniedError(String message) {
		super(ERROR_TYPE, message);
	}

	public AccessDeniedError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
