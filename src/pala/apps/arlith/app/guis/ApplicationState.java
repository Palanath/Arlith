package pala.apps.arlith.app.guis;

import javafx.stage.Stage;
import pala.apps.arlith.app.client.Client;

public class ApplicationState {
	private final Stage stage;
	private final Client client;

	public ApplicationState(Stage stage, Client client) {
		this.stage = stage;
		this.client = client;
	}

	public Stage getStage() {
		return stage;
	}

	public Client getClient() {
		return client;
	}

}
