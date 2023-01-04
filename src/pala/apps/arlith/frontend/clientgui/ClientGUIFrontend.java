package pala.apps.arlith.frontend.clientgui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.login.LogInScene;
import pala.apps.arlith.frontend.themes.arlithdefault.ArlithDefaultTheme;
import pala.apps.arlith.libraries.frontends.Frontend;
import pala.apps.arlith.libraries.frontends.FrontendScene.SceneShowFailureException;
import pala.apps.arlith.libraries.frontends.interfacing.Theme;
import pala.apps.arlith.libraries.frontends.interfacing.UserInterface;

public class ClientGUIFrontend implements Frontend {

	private final Stage stage;
	private final List<Theme> themes = new ArrayList<>();

	public ClientGUIFrontend(Stage stage) {
		this.stage = stage;
		// Add default theme.
		ArlithDefaultTheme theme = new ArlithDefaultTheme();
		themes.add(theme);
		theme.setStage(stage);
	}

	@Override
	public void launch() {
		// Prepare and show initial scene.
		Platform.setImplicitExit(true);
		stage.show();
		try {
			new LogInScene(this, new ArlithClientBuilder()).show();
		} catch (SceneShowFailureException e) {
			ArlithFrontend.getGuiLogger().err("Failed to show the log in scene.");
			ArlithFrontend.getGuiLogger().err(e);
		}
	}

	/**
	 * Gets the ordered, modifiable {@link List} of {@link Theme}s active for this
	 * application. Whenever instantiating a presentation for a GUI, the first
	 * {@link Theme} is queried. If it does not have a presentation for the GUI, the
	 * next is queried, and so on and so forth.
	 * 
	 * @return
	 */
	public List<Theme> getThemes() {
		return themes;
	}

	/**
	 * Queries each of the {@link #getThemes() themes} active in this
	 * {@link ClientGUIFrontend} for a presentation for the specified
	 * {@link UserInterface}. If none of them have a presentaation available,
	 * <code>null</code> is returned. {@link Theme}s are queried in order.
	 * 
	 * @param <P> The presentation type.
	 * @param ui  The {@link UserInterface} for which to create a presentation.
	 * @return The new presentation.
	 */
	public <P> P queryThemes(UserInterface<? super P> ui) {
		P p = null;
		for (Theme t : getThemes())
			if ((p = t.supply(ui)) != null)
				return p;
		return p;
	}

	public Stage getStage() {
		return stage;
	}

}
