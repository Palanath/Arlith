package pala.apps.arlith.frontend.clientgui;

import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.libraries.frontends.Frontend;
import pala.apps.arlith.libraries.frontends.FrontendScene.SceneShowFailureException;

public class ClientGUIFrontend implements Frontend {

	private final Stage stage;

	public ClientGUIFrontend(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void launch() {
		// Prepare and show initial scene.
		try {
			new LogInScene(this, new ArlithClientBuilder()).show();
		} catch (SceneShowFailureException e) {
			ArlithFrontend.getGuiLogger().err("Failed to show the log in scene.");
			ArlithFrontend.getGuiLogger().err(e);
		}
	}

	public Stage getStage() {
		return stage;
	}

}
