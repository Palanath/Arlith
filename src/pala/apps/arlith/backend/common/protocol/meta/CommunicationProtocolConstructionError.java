package pala.apps.arlith.backend.common.protocol.meta;

import pala.libs.generic.json.JSONValue;

public class CommunicationProtocolConstructionError extends RuntimeException {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	private final JSONValue json;

	public JSONValue getJson() {
		return json;
	}

	public CommunicationProtocolConstructionError(String message, Throwable cause, JSONValue json) {
		super(message + "\nCause=" + cause.getMessage() + "\nJSON=<" + json + ">", cause);
		this.json = json;
	}

	public CommunicationProtocolConstructionError(Throwable cause, JSONValue json) {
		super("Cause=" + cause.getMessage() + "\nJSON=<" + json + ">", cause);
		this.json = json;
	}

	public CommunicationProtocolConstructionError(String message, JSONValue json) {
		super(message + "\nJSON=<" + json + ">");
		this.json = json;
	}

	public CommunicationProtocolConstructionError(JSONValue json) {
		super("JSON=<" + json + ">");
		this.json = json;
	}

	public CommunicationProtocolConstructionError() {
		json = null;
	}

	public CommunicationProtocolConstructionError(String message, Throwable cause) {
		super(message, cause);
		json = null;
	}

	public CommunicationProtocolConstructionError(String message) {
		super(message);
		json = null;
	}

	public CommunicationProtocolConstructionError(Throwable cause) {
		super(cause);
		json = null;
	}

}
