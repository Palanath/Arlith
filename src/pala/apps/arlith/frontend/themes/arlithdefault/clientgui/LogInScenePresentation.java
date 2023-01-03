package pala.apps.arlith.frontend.themes.arlithdefault.clientgui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import pala.apps.arlith.frontend.interfaces.clientgui.LogInPresentation;

public class LogInScenePresentation implements LogInPresentation {
	public @FXML BorderPane root;
	public @FXML VBox logInBox;
	public @FXML Text title;
	public @FXML VBox usernamePrompt;
	public @FXML HBox usernamePromptTextSection;
	public @FXML Text usernamePromptText;
	public @FXML Text usernamePromptTextRequiredAsterisk;
	public @FXML TextField usernamePromptField;
	public @FXML VBox passwordPrompt;
	public @FXML HBox passwordPromptTextSection;
	public @FXML Text passwordPromptText;
	public @FXML Text passwordPromptTextAsterisk;
	public @FXML TextField passwordPromptField;;
	public @FXML Button logInButton;

	@Override
	public String getUsername() {
		return usernamePromptText.getText();
	}

	@Override
	public String getPassword() {
		return passwordPromptText.getText();
	}
}
