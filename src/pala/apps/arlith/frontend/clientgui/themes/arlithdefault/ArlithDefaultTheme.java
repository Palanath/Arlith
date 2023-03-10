package pala.apps.arlith.frontend.clientgui.themes.arlithdefault;

import javafx.scene.Scene;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.Theme;
import pala.apps.arlith.frontend.clientgui.themes.arlithdefault.login.LogInPresentationImpl;
import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Presentation;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;

/**
 * <p>
 * Arlith's default {@link Theme}. This class provides a presentation for every
 * user interface that Arlith contains. This {@link Theme} operates presents
 * data using JavaFX and relies on calling code (the frontend) to
 * </p>
 * <p>
 * Presentations under this {@link Theme} show themselves by changing the
 * {@link Scene} of the {@link Stage} that is stored by this {@link Theme}
 * object at the time that the presentation is constructed and returned from
 * {@link #supply(Logic)}.
 * </p>
 * 
 * @author Palanath
 *
 */
public class ArlithDefaultTheme implements Theme {

	@SuppressWarnings("unchecked")
	@Override
	public <P extends Presentation<L>, L extends Logic<P>> P supply(L userInterface) {
		if (userInterface instanceof LogInLogic)
			return (P) new LogInPresentationImpl((LogInLogic) userInterface);
		return null;
	}

}
