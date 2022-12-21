package pala.apps.arlith.backend.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class ServerError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "server";

	public ServerError() {
		super(ERROR_TYPE);
	}

	public ServerError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public ServerError(String message) {
		super(ERROR_TYPE, message);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
