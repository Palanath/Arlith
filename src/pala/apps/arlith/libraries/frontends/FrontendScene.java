package pala.apps.arlith.libraries.frontends;

public interface FrontendScene<F extends Frontend> {
	/**
	 * Returns the {@link Frontend} main-class for the Frontend that this scene is a
	 * part of.
	 * 
	 * @return The {@link Frontend} class that this scene belongs to.
	 */
	F getFrontend();

	/**
	 * Causes this scene to be shown to the user.
	 */
	void show();
}
