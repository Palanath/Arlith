package pala.apps.arlith.libraries.graphics.windows;

import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public interface Page {
	void show(ArlithWindow window) throws WindowLoadFailureException;

	default void cleanup(ArlithWindow window) {

	}
}
