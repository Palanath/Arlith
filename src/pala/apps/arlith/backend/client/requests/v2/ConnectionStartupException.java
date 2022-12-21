package pala.apps.arlith.backend.client.requests.v2;

/**
 * Thrown by a {@link SingleThreadRequestSubsystem} subclass to indicate to the
 * parent's API that starting a connection during a call to
 * {@link SingleThreadRequestSubsystem#prepareConnection()} failed
 * <i>because</i> the actual network connection could not be made.
 * 
 * @author Palanath
 *
 */
public class ConnectionStartupException extends Exception {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public ConnectionStartupException() {
	}

	public ConnectionStartupException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionStartupException(String message) {
		super(message);
	}

	public ConnectionStartupException(Throwable cause) {
		super(cause);
	}

}
