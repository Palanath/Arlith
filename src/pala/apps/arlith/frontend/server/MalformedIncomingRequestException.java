package pala.apps.arlith.frontend.server;

import pala.libs.generic.json.JSONObject;

public class MalformedIncomingRequestException extends Exception {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	private final JSONObject request;

	public MalformedIncomingRequestException(JSONObject request) {
		this.request = request;
	}

	public MalformedIncomingRequestException(String message, JSONObject request) {
		super(message);
		this.request = request;
	}

	public JSONObject getRequest() {
		return request;
	}
}
