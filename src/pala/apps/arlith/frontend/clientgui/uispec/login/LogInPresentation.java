package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.Presentation;

public interface LogInPresentation extends Presentation<LogInLogic> {
	/**
	 * Retrieves the log in identifier that the user has entered to attempt to log
	 * in. This can be either a tag, an email, or a phone number.
	 * 
	 * @return The log in identifier that the user has entered; this is used by the
	 *         user to log in and should be either a tag, an email, or a phone
	 *         number.
	 */
	String getLogInIdentifier();

	/**
	 * Retrieves the password from the user. Called by the interface's logic while
	 * attempting to log the user in <b>or</b> create an account.
	 * 
	 * @return The {@link String} password from the user.
	 */
	String getPassword();

	/**
	 * Retrieves the email from the user for the purposes of creating an account.
	 * 
	 * @return The user's email address.
	 */
	String getEmail();

	/**
	 * Retrieves the phone number from the user for the purposes of creating an
	 * account.
	 * 
	 * @return
	 */
	String getPhoneNumber();

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
