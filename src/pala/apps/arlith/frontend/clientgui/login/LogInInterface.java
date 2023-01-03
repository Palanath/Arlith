package pala.apps.arlith.frontend.clientgui.login;

import pala.apps.arlith.libraries.frontends.interfacing.UserInterface;

public interface LogInInterface extends UserInterface<LogInPresentation> {
	/**
	 * Triggers and performs the log in process. This method should be called
	 * whenever the user attempts to log in.
	 */
	public void triggerLogIn();
}
