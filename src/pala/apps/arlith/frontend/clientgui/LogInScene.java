package pala.apps.arlith.frontend.clientgui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.libraries.frontends.FrontendScene;

public class LogInScene implements FrontendScene<ClientGUIFrontend> {
	private final ClientGUIFrontend frontend;
	private final ArlithClientBuilder builder;
	private Scene scene;

	public LogInScene(ClientGUIFrontend frontend, ArlithClientBuilder builder) {
		this.frontend = frontend;
		this.builder = builder;
	}

	@Override
	public ClientGUIFrontend getFrontend() {
		return frontend;
	}

	@Override
	public void show() throws SceneShowFailureException {
		FXMLLoader loader = new FXMLLoader(LogInScene.class.getResource("LogInGUI.fxml"));
		loader.setController(this);
		Parent root;
		try {
			root = loader.load();
		} catch (IOException e) {
			throw new SceneShowFailureException(e, this);
		}
		scene = new Scene(root);
		frontend.getStage().setScene(scene);
		frontend.getStage().show();
	}

}
