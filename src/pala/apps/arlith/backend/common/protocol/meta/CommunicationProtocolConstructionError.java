package pala.apps.arlith.backend.common.protocol.meta;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.types.CommunicationProtocolType;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * Used to signify that attempting to reconstruct a
 * {@link CommunicationProtocolType} from JSON data failed. This exception is
 * primarily thrown by requests' reconstruction-constructors (that take JSON
 * data representing the request's state and use it to reconstruct themselves)
 * and from requests'
 * {@link Inquiry#receiveResponse(pala.apps.arlith.libraries.networking.Connection)}
 * method, which may throw it while attempting to convert the server's JSON
 * response into a valid {@link CommunicationProtocolType}.
 * </p>
 * <p>
 * Reconstruction-constructors are chiefly run on the server; (the server
 * attempts to reconstruct a client's request from JSON received over the
 * network) and
 * {@link Inquiry#receiveResponse(pala.apps.arlith.libraries.networking.Connection)}
 * is chiefly run on the client (the client sent a request and is attempting to
 * read the server's response and then initialize a
 * {@link CommunicationProtocolType} from it).
 * </p>
 * 
 * @author Palanath
 *
 */
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
