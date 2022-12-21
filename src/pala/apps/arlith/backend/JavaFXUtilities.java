package pala.apps.arlith.backend;

import javafx.application.Platform;

public class JavaFXUtilities {

	public static void runFX(Runnable runnable) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(runnable);
		else
			runnable.run();
	}

}
