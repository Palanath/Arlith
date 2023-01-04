package pala.apps.arlith.frontend.themes.arlithdefault;

import javafx.scene.Scene;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.login.LogInInterface;
import pala.apps.arlith.frontend.themes.arlithdefault.clientgui.LogInScenePresentation;
import pala.apps.arlith.libraries.frontends.interfacing.Theme;
import pala.apps.arlith.libraries.frontends.interfacing.UserInterface;

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
 * {@link #supply(UserInterface)}.
 * </p>
 * 
 * @author Palanath
 *
 */
public class ArlithDefaultTheme implements Theme {

	/**
	 * The {@link Stage} upon which to show new presentations.
	 */
	private Stage stage;

	public Stage getStage() {
		return stage;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P> P supply(UserInterface<? super P> userInterface) {
		if (userInterface instanceof LogInInterface)
			return (P) new LogInScenePresentation(stage, (LogInInterface) userInterface);
		return null;
	}

}
