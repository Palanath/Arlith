package pala.apps.arlith.frontend.clientgui;

import javafx.scene.Scene;
import javafx.stage.Stage;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface Presentation<U extends Logic<? extends Presentation<? super U>>> {
	/**
	 * <p>
	 * Gets the {@link Scene} upon which this {@link Presentation} displays itself.
	 * This {@link Scene} can be given to a {@link Stage}.
	 * </p>
	 * <p>
	 * Querying the {@link Scene} for the first time may cause it to be generated
	 * through the course of execution of this method. Such can result in an error,
	 * which will be reflected by a thrown {@link WindowLoadFailureException}.
	 * Subsequent calls always return the same {@link Scene}.
	 * </p>
	 * 
	 * @return The {@link Scene} backed by this {@link Presentation}.
	 */
	Scene getScene() throws WindowLoadFailureException;
}
