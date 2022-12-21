package pala.apps.arlith.frontend.guis.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import pala.apps.arlith.backend.Utilities;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.libs.generic.guis.ApplicationProperties;
import pala.libs.generic.guis.Window;

public class LaunchSettingsWindow extends Window {
	private @FXML TextField localAddress, localPort, serverAddress, serverPort;
	private @FXML Button launchServerButton;

	private @FXML void initialize() {
		String port = String.valueOf(Utilities.DEFAULT_PORT);
		localPort.setText(port);
		serverAddress.setText(Utilities.DEFAULT_DESTINATION_ADDRESS);
		serverPort.setText(port);
	}

	private @FXML void launchServer() {
		boolean completed = false;
		try {
			launchServerButton.setDisable(true);
			ArlithServer server = new ArlithServer();
			server.setPort(Integer.parseInt(localPort.getText().trim()));
			server.start();
			completed = true;
			System.out.println("Successfully launched the server. You'll need to close the program to stop it.");
		} catch (IOException e) {
			System.err.println("An error occurred while trying to launch the server.");
			e.printStackTrace();
		} finally {
			launchServerButton.setDisable(!completed);
		}
	}

	private @FXML void setClientEndpoint() {
		if (!serverAddress.getText().trim().isEmpty())
			Utilities.setPreferredDestinationAddress(serverAddress.getText().trim());
		if (!serverAddress.getText().trim().isEmpty())
			Utilities.setPreferredPort(Integer.parseInt(serverPort.getText().trim()));
		System.out.println("Client endpoint set successfully.");
	}

	@Override
	public void destroy() {

	}

	@Override
	protected void show(Stage stage, ApplicationProperties properties) throws WindowLoadFailureException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("LaunchSettings.fxml"));
		loader.setController(this);
		try {
			Scene scene = new Scene(loader.load());
			scene.getStylesheets().add(properties.themeStylesheet.get());
			stage.setScene(scene);
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}

	}
}
