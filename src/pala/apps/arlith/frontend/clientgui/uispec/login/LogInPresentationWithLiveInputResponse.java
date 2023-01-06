package pala.apps.arlith.frontend.clientgui.uispec.login;

/**
 * Specifies a presentation for the Log In scene's user interface that
 * additionally supports live, responsive feedback for user input. The
 * {@link LogInLogic} implementation will determine whether the presentation
 * implementation is an instance of this class and respond accordingly.
 * 
 * @author Palanath
 *
 */
public interface LogInPresentationWithLiveInputResponse extends LogInPresentation {
	interface Issue {
		Severity getSeverity();
	}

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
		WARNING, ERROR;
	}

	/**
	 * An {@link Severity#ERROR} where a duplicate hashtag is contained within the
	 * username. The {@link #getCharpos() character position} stored by the
	 * {@link Issue} is the location of the second hashtag, independent of where the
	 * first is and of whether there are more than two hashtags.
	 * 
	 * @author Palanath
	 *
	 */
	class DuplicateHashtagIssue implements Issue {
		private final int charpos;

		public DuplicateHashtagIssue(int charpos) {
			this.charpos = charpos;
		}

		public int getCharpos() {
			return charpos;
		}

		@Override
		public Severity getSeverity() {
			return Severity.ERROR;
		}
	}

	/**
	 * Represents something that is wrong with the username. This enum holds issues
	 * that are constant and do not have properties or vary from instance to
	 * instance (there is only one instance of each issue). All issues contained in
	 * this enum type have {@link Severity} {@link Severity#ERROR}.
	 * 
	 * @author Palanath
	 *
	 */
	enum IssueType implements Issue {
		USERNAME_TOO_SHORT, USERNAME_EMPTY, USERNAME_TOO_LONG, PASSWORD_TOO_SHORT, PASSWORD_EMPTY;

		@Override
		public Severity getSeverity() {
			return Severity.ERROR;
		}
	}

	/**
	 * <p>
	 * Returns the issue with the username, if any, or <code>null</code> if there is
	 * no issue. This should be called while the user enters a username, upon each
	 * change to the username-string contents of the field in which they enter the
	 * username. As the user types, any {@link Issue} with the username is reported
	 * and should be displayed to the user in a non-intrusive and non-interrupting
	 * way.
	 * </p>
	 * <p>
	 * The {@link Issue} returned is shown to the user by changing the color of the
	 * text input (e.g. to red for {@link Severity#ERROR} {@link Issue}s and gold to
	 * {@link Severity#WARNING} issues) and showing similarly colored text above the
	 * input depicting what is wrong with the username as they have typed it so far.
	 * </p>
	 * 
	 * @return The current {@link Issue} with the typed username.
	 */
	Issue triggerCheckUsername();
}
