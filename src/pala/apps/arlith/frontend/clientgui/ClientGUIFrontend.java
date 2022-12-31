package pala.apps.arlith.frontend.clientgui;

import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.libraries.frontends.Frontend;

public class ClientGUIFrontend implements Frontend {

	private final Stage stage;

	public ClientGUIFrontend(Stage stage) {
		this.stage = stage;
	}

	@Override
	public void launch() {
		// Prepare and show initial scene.
		new LogInScene(this, new ArlithClientBuilder()).show();
	}

	public Stage getStage() {
		return stage;
	}

}
