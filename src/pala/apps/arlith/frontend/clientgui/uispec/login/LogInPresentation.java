package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.Presentation;

/**
 * <p>
 * Outline class for the presentation of the initial scene shown to the user.
 * This scene allows the user to log in or create an account. This scene is
 * responsible for obtaining the information needed to <i>authenticate</i> the
 * user.
 * </p>
 * <p>
 * Authentication in can be done either through (1) a uniquely identifying
 * <i>log-in identifier</i> and password or through (2) a username, an email
 * address, a password, and optionally, a phone number. In case (1), the
 * information is used to log the user in to an already existing account, and in
 * case (2), the information is used to create the user a new account.
 * </p>
 * <p>
 * Typically, this information is presented to the user by two, distinct menus:
 * </p>
 * <ul>
 * <li>One for <em>Logging In</em> which contains:
 * <ul>
 * <li>A prompt for the user's username/email/phone-number</li>
 * <li>A password prompt</li>
 * </ul>
 * </li>
 * <li>One for <em>Account Creation</em> which contains:
 * <ul>
 * <li>A username prompt</li>
 * <li>An email prompt</li>
 * <li>A phone number prompt (which can be left blank)</li>
 * <li>and a password prompt</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author Palanath
 *
 */
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
	 * Gets a user's username for the purposes of creating an account.
	 * 
	 * @return
	 */
	String getUsername();

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
	 * <p>
	 * Indicates to the user that the specified type of problem arose while
	 * attempting to log the user in.
	 * </p>
	 * 
	 * @param problem The type of problem that occurred.
	 */
	void showLoginProblem(LoginProblemValue problem);

	/**
	 * <p>
	 * Shows to the user that a log in attempt failed. This is invoked not because
	 * the server actually rejected the log in but because something actually went
	 * wrong during the connection to the server. It is also called whenever the
	 * client does not understand the response from the server, however (which can
	 * occur in cases of a version mismatch or similar). When this is called, the
	 * typical approach is to allow the user to try to log in again, which would
	 * cause the logic to try and reconnect to the server.
	 * 
	 * @param error The specific error that occurred.
	 */
	void showLogInFailure(LoginFailureException error);

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
