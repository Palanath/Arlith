package pala.apps.arlith.backend.client;

/**
 * Signifies that the response that a server gave was malformed such that the
 * client was not able to understand the response from the server.
 * 
 * @author Palanath
 *
 */
public class MalformedServerResponseException extends RuntimeException {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	protected MalformedServerResponseException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MalformedServerResponseException() {
	}

	public MalformedServerResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedServerResponseException(String message) {
		super(message);
	}

	public MalformedServerResponseException(Throwable cause) {
		super(cause);
	}

}
