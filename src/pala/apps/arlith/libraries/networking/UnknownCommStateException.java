package pala.apps.arlith.libraries.networking;

import java.io.IOException;

/**
 * Used to signify that the state of a {@link Connection}, regarding connection
 * between its endpoint, is unknown. This can be the result of a malformed
 * block, or if operations on the {@link Connection}'s underlying stream throw
 * an {@link IOException}. Any time the class cannot guarantee that a whole
 * block was fully read will also result in an
 * {@link UnknownCommStateException}.
 * 
 * @author Palanath
 *
 */
public class UnknownCommStateException extends Exception {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public UnknownCommStateException() {
	}

	public UnknownCommStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownCommStateException(String message) {
		super(message);
	}

	public UnknownCommStateException(Throwable cause) {
		super(cause);
	}

}
