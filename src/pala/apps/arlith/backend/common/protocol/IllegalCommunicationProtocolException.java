package pala.apps.arlith.backend.common.protocol;

import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;

/**
 * Thrown when the server responds to a request with an error that it should not
 * reply with.
 * 
 * @author Palanath
 *
 */
public class IllegalCommunicationProtocolException extends RuntimeException {

	/**
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public IllegalCommunicationProtocolException(String message, CommunicationProtocolError error) {
		super(message, error);
	}

	public IllegalCommunicationProtocolException(CommunicationProtocolError e) {
		super("Illegal CommunicationProtocolError found: " + e, e);
	}

	@Override
	public synchronized CommunicationProtocolError getCause() {
		return (CommunicationProtocolError) super.getCause();
	}

}
