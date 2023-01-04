package pala.apps.arlith.frontend.clientgui.login;

import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface LogInPresentation {
	/**
	 * Retrieves the username from the user. Called by the interface's logic while
	 * attempting to log the user in.
	 * 
	 * @return The {@link String} username from the user.
	 */
	String getUsername();

	/**
	 * Retrieves the password from the user. Called by the interface's logic while
	 * attempting to log the user in.
	 * 
	 * @return The {@link String} password from the user.
	 */
	String getPassword();

	/**
	 * Indicates to the user that the specified type of problem arose while
	 * attempting to log the user in.
	 * 
	 * @param problem The type of problem that occurred.
	 */
	void showLoginProblem(LoginProblemValue problem);

	/**
	 * Prevents the user from triggering a <code>log-in</code> (via
	 * {@link LogInInterface#triggerLogIn()}) until {@link #unlockUIForLoggingIn()}
	 * is called. The UI should remain responsive so as to indicate to the user that
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
