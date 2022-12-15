package pala.apps.arlith.api.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class InvalidConnectionStateError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "invalid-connection-state";

	public InvalidConnectionStateError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public InvalidConnectionStateError(String message) {
		super(ERROR_TYPE, message);
	}

	public InvalidConnectionStateError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
