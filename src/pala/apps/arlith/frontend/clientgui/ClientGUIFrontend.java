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
		// Prepare initial scene.
		ArlithClientBuilder builder = new ArlithClientBuilder();
		LogInScene lis = new LogInScene(this, builder);
		lis.show();// Show initial scene.
	}

	public Stage getStage() {
		return stage;
	}

}
