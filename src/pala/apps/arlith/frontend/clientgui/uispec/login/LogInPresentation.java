package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.Presentation;

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
