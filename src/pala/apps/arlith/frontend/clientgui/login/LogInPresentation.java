package pala.apps.arlith.frontend.clientgui.login;

import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface LogInPresentation {
	/**
	 * Retrieves the username from the user. Used for logging in.
	 * 
	 * @return The {@link String} username from the user.
	 */
	String getUsername();

	/**
	 * Retrieves the password from the user. Used for logging in.
	 * 
	 * @return The {@link String} password from the user.
	 */
	String getPassword();

	/**
	 * Enables this presentation to start receiving user input so that it may
	 * trigger {@link LogInInterface#triggerLogIn()} at the user's request. <i>This
	 * method is typically provided in presentation interfaces for UIs that accept
	 * user input.</i> This method is typically called by the corresponding
	 * interface type ({@link LogInInterface}) so that it can point out to the
	 * presentation that it is ready for the presentation to display itself to the
	 * user. If this method is called more than once, the behavior of any of the
	 * subsequent invocations is undefined. Additionally, if this method is called
	 * after {@link #hide()}, its behavior is undefined.
	 * 
	 * @throws WindowLoadFailureException If the presentation fails to show to the
	 *                                    user for some reason.
	 */
	void show() throws WindowLoadFailureException;

	/**
	 * Causes this presentation to stop showing to the user. This is typically
	 * called when the UI needs to be closed or a different scene is being switched
	 * to. The presentation should perform any cleanup necessary upon invocation of
	 * this method. If this method is called more than once, the behavior of any of
	 * the subsequent invocations is undefined. Additionally, if this method is
	 * called before {@link #show(LogInInterface)}, its behavior is undefined.
	 */
	void hide();
}
