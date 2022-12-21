package pala.apps.arlith.backend.networking.encryption;

public class MalformedResponseException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public MalformedResponseException() {
	}

	public MalformedResponseException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedResponseException(String message) {
		super(message);
	}

	public MalformedResponseException(Throwable cause) {
		super(cause);
	}

}
