package pala.apps.arlith.app.guis.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.app.server.Server;

public class LaunchSettingsWindow {
	private @FXML TextField localAddress, localPort, serverAddress, serverPort;
	private @FXML Button launchServerButton;
	private final LogInWindow source;

	private @FXML void initialize() {
		String port = String.valueOf(Utilities.DEFAULT_PORT);
		localPort.setText(port);
		serverAddress.setText(Utilities.DEFAULT_DESTINATION_ADDRESS);
		serverPort.setText(port);
	}

	public LaunchSettingsWindow(LogInWindow source) {
		this.source = source;
	}

	private @FXML void launchServer() {
		boolean completed = false;
		try {
			launchServerButton.setDisable(true);
			Server server = new Server();
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
}
