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

	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckUsername();

	@Optional(type = LogInPresentationWithLiveInputResponse.class)
	void triggerCheckLogInIdentifier();

}
