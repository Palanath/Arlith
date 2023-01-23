package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Optional;
import pala.apps.arlith.frontend.clientgui.Presentation;

/**
 * <p>
 * Specifies a presentation for the Log In scene's user interface that
 * additionally supports live, responsive feedback for user input. The
 * {@link LogInLogic} implementation will determine whether the presentation
 * implementation is an instance of this class and respond accordingly.
 * </p>
 * <p>
 * Generally, if a presentation is an instance of
 * {@link LogInPresentationWithLiveInputResponse}, it invokes
 * {@link LogInLogic#triggerCheckLogInIdentifier()} and other, similar,
 * {@link Optional} "trigger check" methods. In each of these methods, the
 * {@link Logic} checks the corresponding input data from the presentation (by
 * calling {@link LogInPresentation#getLogInIdentifier()}, or similar), and will
 * call the presentation's
 * {@link LogInPresentationWithLiveInputResponse#showLogInIdentifierError(Issue)}
 * method, or corresponding "show error" methods for other types of inputs. The
 * presentation is then expected to display the error to the user.
 * </p>
 * <p>
 * This exchange is expected to happen <i>live</i>, i.e., while the user is
 * providing input (per character entered).
 * </p>
 * 
 * @author Palanath
 *
 */
public interface LogInPresentationWithLiveInputResponse extends LogInPresentation {
	/**
	 * <p>
	 * Represents an {@link Issue} with one of the inputs in the log in GUI. This
	 * single type is used to represent issues for each of the inputs.
	 * </p>
	 * 
	 * @author Palanath
	 *
	 */
	interface Issue {
		/**
		 * Indicates the {@link Severity} of the {@link Issue}. The {@link Severity} has
		 * effects as to how the {@link Presentation} should treat the issue. For
		 * example, the {@link Presentation} should not the user to continue and submit
		 * data from an input where an {@link Issue} of {@link Severity#ERROR} exists,
		 * whereas, although both {@link Severity#ERROR} and {@link Severity#WARNING}
		 * issues are displayed to the user, {@link Severity#WARNING} issues do not bar
		 * the user from submitting input data.
		 * 
		 * @return The {@link Issue}'s {@link Severity}.
		 */
		Severity getSeverity();

		/**
		 * <p>
		 * A short, descriptive message of the issue, suitable for showing the user when
		 * the issue occurs.
		 * </p>
		 * <p>
		 * Every issue has a corresponding message. This method will never return
		 * <code>null</code>.
		 * </p>
		 * 
		 * @return A short, descriptive {@link String} describing what the issue is. The
		 *         message is always fewer than 128 characters in length.
		 */
		String message();

		/**
		 * <p>
		 * The position in the corresponding string of the issue, if the issue
		 * corresponds to a character of substring of the input string, or
		 * <code>-1</code> if positional information is not available for this type of
		 * issue or is not supported. Consider the case that the {@link Issue} regards
		 * an email address containing two periods (<code>.</code> characters) in the
		 * local part (the part before the <code>@</code> symbol); the result of this
		 * method would be the position of the second (offending) period, since this
		 * character is what causes the email to be invalid.
		 * </p>
		 * <p>
		 * If a whole, multiple-character substring in the email address (or other input
		 * that this {@link Issue} corresponds to) is the cause of this {@link Issue},
		 * the returned character position points to the first character in that
		 * substring.
		 * </p>
		 * 
		 * @return The (starting) position of the offending substring in the input that
		 *         this {@link Issue} corresponds to.
		 */
		int charpos();
	}

	/**
	 * <p>
	 * Shows the provided {@link Issue} to the user, or indicates to the user that
	 * the log in identifier input is valid if this method is provided
	 * <code>null</code>. The {@link Issue} provided, if any, represents a problem
	 * with the {@link LogInPresentation#getLogInIdentifier()} input. The
	 * {@link Issue} should be shown to the user in the context of that input.
	 * </p>
	 * 
	 * @param issue An object containing details about the issue with the input, or
	 *              <code>null</code> if the input is valid.
	 */
	void showLogInIdentifierError(Issue issue);

	/**
	 * <p>
	 * Shows the provided {@link Issue} to the user in the context of the username
	 * input or conveys to the user that the input is not invalid if the
	 * {@link Issue} parameter is <code>null</code>. The {@link Issue} provided, if
	 * any, represents a problem with the {@link LogInPresentation#getUsername()}
	 * input.
	 * </p>
	 * 
	 * @param issue An object containing details about the issue with the input, or
	 *              <code>null</code> if the input is valid.
	 */
	void showUsernameError(Issue issue);

	// TODO Fill out remaining methods.

	/**
	 * Indicates the {@link Severity} of an {@link Issue}. {@link Issue}s with
	 * either severity should be shown to the user, but {@link Issue}s with severity
	 * {@link #ERROR} should prevent the user from being able to submit the log in
	 * form.
	 * 
	 * @author Palanath
	 *
	 */
	enum Severity {
		/**
		 * Indicates to the user that there is some issue with the provided input. The
		 * user may still continue and submit the form containing the input anyway.
		 * Issues with this {@link Severity} are merely advisory.
		 */
		WARNING,
		/**
		 * Indicates to the user that there is some issue with the provided input that
		 * is severe enough to bar the user from being able to submit the form. It is
		 * best if {@link Issue}s of this {@link Severity} are distinguished from those
		 * of {@link #WARNING} quickly (this is typically done through a color coding).
		 */
		ERROR;
	}

}
