package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Optional;

public interface LogInLogic extends Logic<LogInPresentation> {
	/**
	 * <p>
	 * Triggers and performs the log in process. This method should be called
	 * whenever the user attempts to log in.
	 * </p>
	 * <p>
	 * If the log in is successful, this method schedules the home window to be
	 * shown and returns.
	 * </p>
	 */
	void triggerLogIn();

	/**
	 * <p>
	 * Triggers and performs the account-creation process. This logs the user in.
	 * </p>
	 * <p>
	 * If the log in is successful, this method schedules the home window to be
	 * shown and returns.
	 * </p>
	 */
	void triggerCreateAccount();

	/**
	 * <h1>Username Check Trigger</h1>
	 * <p>
	 * Called by {@link LogInPresentationWithLiveInputResponse} to validate the
	 * syntax of the username being input by the user, live. This method should be
	 * called whenever the user updates the username input so that the
	 * {@link LogInPresentationWithLiveInputResponse} can be notified of the
	 * validity of the changed input string.
	 * </p>
	 * <h2>Lazy Notification</h2>
	 * <p>
	 * The very first call to this method will invoke
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * with the appropriate argument. Subsequent calls <i>may</i> or <i>may not</i>
	 * invoke
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * if the result of calls to this method would provide an equal argument to
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * as it was last invoked by this method, but are guaranteed to call
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * if the argument will be distinct from the last argument provided in an
	 * invocation of
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * by this method. This essentially means that this method may choose not to
	 * repeatedly call
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * if doing so conveys no new information to the
	 * {@link LogInPresentationWithLiveInputResponse}. For example, if a user has
	 * entered in the username input <code>#pala</code> and they then change that
	 * input to <code>#palan</code> (adding the character <code>n</code> after
	 * <code>#pala</code>). Since the issue with <code>#pala</code> is the same as
	 * the issue with <code>#palan</code> (the hashtag (<code>#</code>) is not
	 * allowed in either case), this method may choose not to invoke
	 * {@link LogInPresentationWithLiveInputResponse#showUsernameError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue)}
	 * since the issue with the username has not changed.
	 * </p>
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckUsername();

	/**
	 * <p>
	 * Called by {@link LogInPresentationWithLiveInputResponse} to validate the
	 * syntax of the log in identifier input, live. This method should be called
	 * whenever the user updates the log in identifier input so that the
	 * {@link LogInPresentationWithLiveInputResponse} can be notified of the
	 * validity of the changed input string.
	 * </p>
	 * <p>
	 * This method performs <i>lazy notification</i>. For details, see
	 * {@link #triggerCheckUsername()}.
	 * </p>
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckLogInIdentifier();

	/**
	 * <p>
	 * Called by {@link LogInPresentationWithLiveInputResponse} to validate the
	 * syntax of the password input, live. This method should be called whenever the
	 * user updates the password input so that the
	 * {@link LogInPresentationWithLiveInputResponse} can be notified of the
	 * validity of the changed input string.
	 * </p>
	 * <p>
	 * This method performs <i>lazy notification</i>. For details, see
	 * {@link #triggerCheckUsername()}.
	 * </p>
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckPassword();

	/**
	 * <p>
	 * Called by {@link LogInPresentationWithLiveInputResponse} to validate the
	 * syntax of the email input, live. This method should be called whenever the
	 * user updates the email input so that the
	 * {@link LogInPresentationWithLiveInputResponse} can be notified of the
	 * validity of the changed input string.
	 * </p>
	 * <p>
	 * This method performs <i>lazy notification</i>. For details, see
	 * {@link #triggerCheckUsername()}.
	 * </p>
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckEmail();

	/**
	 * <p>
	 * Called by {@link LogInPresentationWithLiveInputResponse} to validate the
	 * syntax of the phone number input, live. This method should be called whenever
	 * the user updates the phone number input so that the
	 * {@link LogInPresentationWithLiveInputResponse} can be notified of the
	 * validity of the changed input string.
	 * </p>
	 * <p>
	 * This method performs <i>lazy notification</i>. For details, see
	 * {@link #triggerCheckUsername()}.
	 * </p>
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckPhoneNumber();

}
