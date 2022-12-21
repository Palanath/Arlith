package pala.apps.arlith.frontend.guis;

import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClient;

public class ApplicationState {
	private final Stage stage;
	private final ArlithClient client;

	public ApplicationState(Stage stage, ArlithClient client) {
		this.stage = stage;
		this.client = client;
	}

	public Stage getStage() {
		return stage;
	}

	public ArlithClient getClient() {
		return client;
	}

}
