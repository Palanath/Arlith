package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.Presentation;

/**
 * <p>
 * Specification for the presentation of the Log In scene's UI to the user. This
 * type specifies all of the methods and functionality that the <i>logic</i> of
 * the UI will need to be able to invoke on whatever presentation information is
 * showing (presenting) the UI to the end-user. The presentation implementation
 * should support the methods specified in this type so that the logic can
 * effectively engage with the end-user.
 * </p>
 * <p>
 * In addition to the abstract methods in this interface, the implementation
 * should also:
 * </p>
 * <ol>
 * <li>Invoke the logic's {@link LogInLogic#triggerLogIn()} method whenever the
 * user attempts to log in. (What constitutes the user attempting to log in is
 * determined by the presentation implementation.)</li>
 * </ol>
 * 
 * @author Palanath
 *
 */
public interface LogInPresentation extends Presentation<LogInLogic> {
	/**
	 * Retrieves the username from the user. Called by the interface's logic while
	 * attempting to log the user in.
	 * 
	 * @return The {@link String} username from the user.
	 */
	String getUsername();

	/**
	 * Retrieves the password from the user. Called by the interface's logic while
	 * attempting to log the user in.
	 * 
	 * @return The {@link String} password from the user.
	 */
	String getPassword();

	/**
	 * Indicates to the user that the specified type of problem arose while
	 * attempting to log the user in.
	 * 
	 * @param problem The type of problem that occurred.
	 */
	void showLoginProblem(LoginProblemValue problem);

	/**
	 * Prevents the user from triggering a <code>log-in</code> (via
	 * {@link LogInLogic#triggerLogIn()}) until {@link #unlockUIForLoggingIn()} is
	 * called. The UI should remain responsive so as to indicate to the user that
	 * the application is still alive, and should, preferably, also indicate to the
	 * user that the application is currently attempting to log in. If this method
	 * is called more than once before calling {@link #unlockUIForLoggingIn()}, the
	 * subsequent calls have no effect.
	 */
	void lockUIForLoggingIn();

	/**
	 * Releases the restriction that bars the user from triggering a
	 * <code>log-in</code> (essentially, undoes the effects of
	 * {@link #lockUIForLoggingIn()}). This method is called by the Log In scene's
	 * logic class while the client attempts to log in to the server, so that
	 */
	void unlockUIForLoggingIn();
}
