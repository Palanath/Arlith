package pala.apps.arlith.frontend.clientgui.uispec.login;

import javafx.beans.InvalidationListener;
import javafx.scene.control.TextField;
import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Optional;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation.Input;

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
	 * <p>
	 * Called to cause the logic to check the specified input. This should be called
	 * when the user changes the data currently in the input.
	 * </p>
	 * <p>
	 * Arlith's default theme uses a {@link TextField} for inputs and calls this
	 * method any time one of the {@link TextField}s'
	 * {@link TextField#textProperty()} changes (by registering an
	 * {@link InvalidationListener}).
	 * </p>
	 * <p>
	 * When this trigger is invoked, the logic will verify the syntactic validity of
	 * the specified input and inform the presentation as appropriate (by calling
	 * {@link LogInPresentationWithLiveInputResponse#showInputError(pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue, Input)}
	 * or {@link LogInPresentationWithLiveInputResponse#showInputValid(Input)}).
	 * </p>
	 * <p>
	 * Note that the logic might not call one of the aforementioned methods if the
	 * information being displayed to the user still holds. For example, if the user
	 * is entering an email and the input changes from <code>a</code> to
	 * <code>ab</code>, both strings are syntactically invalid emails for the same
	 * reason: both are missing the <code>@</code> symbol, so the logic may choose
	 * to let the previous issue being displayed to the user persist.
	 * </p>
	 * 
	 * @param input The {@link Input} that needs to be checked.
	 */
	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckInput(Input input);

}
