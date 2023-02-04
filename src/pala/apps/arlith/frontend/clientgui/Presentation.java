package pala.apps.arlith.frontend.clientgui;

import javafx.stage.Stage;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface Presentation<L extends Logic<? extends Presentation<? super L>>> {
	/**
	 * Shows this {@link Presentation} to the user on the specified {@link Stage}.
	 * This method loads the presentation's graphical elements and displays them
	 * onto the specified {@link Stage}. If an error of any kind occurs, it is
	 * returned back either as, or wrapped in, a {@link WindowLoadFailureException}.
	 * 
	 * @param stage The {@link Stage} to show this {@link Presentation} on.
	 * @throws WindowLoadFailureException If an issue arises.
	 */
	void show(Stage stage) throws WindowLoadFailureException;

	/**
	 * Cleans up this {@link Presentation} so that
	 */
	default void dispose() {

	}
}
