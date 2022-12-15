package pala.apps.arlith.api.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class AlreadyInCommunityError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public AlreadyInCommunityError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public AlreadyInCommunityError(String message) {
		super(ERROR_TYPE, message);
	}

	public AlreadyInCommunityError() {
		super(ERROR_TYPE);
	}

	public static final String ERROR_TYPE = "already-in-community";

	@Override
	protected void build(JSONObject object) {
	}

}
