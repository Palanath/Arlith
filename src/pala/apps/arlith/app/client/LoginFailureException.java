package pala.apps.arlith.app.client;

/**
 * Signifies that an error was encountered while trying to log in. Typically, a
 * retry is made, (perhaps after a delay), after this exception is thrown. This
 * exception is meant to signify that something actually went wrong during the
 * connection, not that the server did not want to authenticate you. It is,
 * however, also thrown when the client does not understand what the server
 * sends (which can be the result of the server having a different version than
 * the client). Rather than a connection error occurring in this case, the
 * server may try to throttle the connection if subsequent attempts are made, if
 * the client does not understand the response.
 * 
 * @author Palanath
 *
 */
public class LoginFailureException extends Exception {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public LoginFailureException() {
	}

	public LoginFailureException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoginFailureException(String message) {
		super(message);
	}

	public LoginFailureException(Throwable cause) {
		super(cause);
	}

}
