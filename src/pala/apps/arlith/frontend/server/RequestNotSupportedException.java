package pala.apps.arlith.frontend.server;

import pala.libs.generic.json.JSONObject;

public class RequestNotSupportedException extends Exception {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	private final JSONObject request;

	public RequestNotSupportedException(JSONObject json) {
		this.request = json;
	}

	public RequestNotSupportedException(String message, JSONObject request) {
		super(message);
		this.request = request;
	}

	public JSONObject getRequest() {
		return request;
	}

}
