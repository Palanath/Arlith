package pala.apps.arlith.frontend.clientgui;

import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend.UserInterface;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface Presentation<L extends Logic<? extends Presentation<? super L>>> {
	/**
	 * <p>
	 * Shows this {@link Presentation} to the user on the specified {@link Stage}.
	 * This method loads the presentation's graphical elements and displays them
	 * onto the specified {@link Stage}. If an error of any kind occurs, it is
	 * returned back either as, or wrapped in, a {@link WindowLoadFailureException}.
	 * </p>
	 * <p>
	 * This method is/(should be) called on the JavaFX thread by default. It is
	 * called by the {@link UserInterface} managing the UI this {@link Presentation}
	 * is a part of, but that {@link UserInterface}'s
	 * {@link UserInterface#show(Stage)} method (which is what is expected to call
	 * this method) is called on the JavaFX thread by the {@link UserInterface}
	 * framework. See the {@link UserInterface} class for more details.
	 * </p>
	 * 
	 * @param stage The {@link Stage} to show this {@link Presentation} on.
	 * @throws WindowLoadFailureException If an issue arises.
	 */
	void show(Stage stage) throws WindowLoadFailureException;

	/**
	 * Cleans up this {@link Presentation}. Note that this might not be called on
	 * the JavaFX thread.
	 */
	default void dispose() {

	}
}
