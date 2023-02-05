package pala.apps.arlith.frontend.clientgui.uispec.login;

import javafx.scene.text.Text;
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
	 * Enumeration over the types of inputs that this UI allows the user to provide
	 * data for.
	 * 
	 * @author Palanath
	 *
	 */
	enum Input {
		/**
		 * The log in identifier used to log in. This is either a user tag (e.g.
		 * Joe#1243), an email (e.g. joe@example.com), or a phone number (e.g.
		 * 5559871234).
		 */
		LOGIN_IDENTIFIER,
		/**
		 * The password used for both logging in and creating an account.
		 */
		PASSWORD,
		/**
		 * The username, used for creating an account. Note that this input does not
		 * accept a user <i>tag</i>, which is a username followed by a hashtag then a
		 * discriminator, e.g. (Joe#1243).
		 */
		USERNAME,
		/**
		 * The email address, used for creating an account.
		 */
		EMAIL_ADDRESS,
		/**
		 * The phone number, used for creating an account. This input is optional; the
		 * user does not have to provide it.
		 */
		PHONE_NUMBER
	}

	/**
	 * Gets the textual data that the user has entered for the provided input.
	 * 
	 * @param input The input to get the data of.
	 * @return The data the user has entered into the specified input.
	 */
	String getInputValue(Input input);

	/**
	 * <p>
	 * Indicates that the server rejected the user's log in information for the
	 * specified reason.
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
	 * </p>
	 * 
	 * @param error The specific error that occurred.
	 */
	void showLogInFailure(LoginFailureException error);

	/**
	 * <p>
	 * Shows to the user that the log in attempt failed for some other,
	 * logic-specifiable reason. This is used to allow the logic to convert complex
	 * errors into a user-formatted string that the presentation should be able to
	 * display, at the cost of disallowing the presentation to know the details, (so
	 * auxiliary information cannot be presented to the user, e.g. color coding).
	 * The string may be as long as a small paragraph (~50 words).
	 * </p>
	 * <p>
	 * This type of error is presented to the user in Arlith's default theme in the
	 * same way (same {@link Text} object) as
	 * {@link #showLoginProblem(LoginProblemValue)} and
	 * {@link #showLogInFailure(LoginFailureException)}, (although with a different
	 * color).
	 * </p>
	 * 
	 * @param error The error to display to the user. This is usually, but not
	 *              necessarily, something severe.
	 */
	void showLogInError(String error);

	void showCreateAccountError(String error);

	/**
	 * Prevents the user from attempting to log in or create an account until
	 * {@link #unlockUI()} is called. The UI should remain responsive so as to
	 * indicate to the user that the application is still alive, and should,
	 * preferably, also indicate to the user that the application is currently
	 * attempting to log in. If this method is called more than once before calling
	 * {@link #unlockUI()}, the subsequent calls have no effect.
	 */
	void lockUI();

	/**
	 * Releases the restriction that bars the user from triggering log in or account
	 * creation (essentially, undoes the effects of {@link #lockUI()}). This method
	 * is called by the Log In scene's logic class while the client attempts to log
	 * in to the server, so that the user does not repeatedly cause multiple
	 * simultaneous log in requests, and so that the GUI can update the user of the
	 * fact that a request is being made.
	 */
	void unlockUI();
}
