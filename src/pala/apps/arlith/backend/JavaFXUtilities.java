package pala.apps.arlith.backend;

import javafx.application.Platform;

/**
 * Utilities class for general utilities related to JavaFX. See
 * {@link Utilities} for non-JFX utilities.
 * 
 * @author Palanath
 *
 */
public class JavaFXUtilities {

	public static void runFX(Runnable runnable) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(runnable);
		else
			runnable.run();
	}

}
