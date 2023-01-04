package pala.apps.arlith.frontend.clientgui.uispec.login;

import pala.apps.arlith.frontend.clientgui.Logic;

public interface LogInLogic extends Logic<LogInPresentation> {
	/**
	 * Triggers and performs the log in process. This method should be called
	 * whenever the user attempts to log in.
	 */
	public void triggerLogIn();
}