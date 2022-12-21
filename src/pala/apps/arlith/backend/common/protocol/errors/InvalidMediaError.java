package pala.apps.arlith.backend.common.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class InvalidMediaError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "invalid-media";

	public InvalidMediaError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public InvalidMediaError(String message) {
		super(ERROR_TYPE, message);
	}

	public InvalidMediaError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
