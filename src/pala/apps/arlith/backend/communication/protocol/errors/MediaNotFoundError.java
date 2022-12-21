package pala.apps.arlith.backend.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;

public class MediaNotFoundError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "media-not-found";

	public MediaNotFoundError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public MediaNotFoundError(String message) {
		super(ERROR_TYPE, message);
	}

	public MediaNotFoundError() {
		super(ERROR_TYPE);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
