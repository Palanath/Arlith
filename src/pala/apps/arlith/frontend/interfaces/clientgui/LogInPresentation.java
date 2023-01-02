package pala.apps.arlith.frontend.interfaces.clientgui;

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
}
