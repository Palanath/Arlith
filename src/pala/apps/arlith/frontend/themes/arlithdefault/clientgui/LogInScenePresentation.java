package pala.apps.arlith.frontend.themes.arlithdefault.clientgui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.login.LogInInterface;
import pala.apps.arlith.frontend.clientgui.login.LogInPresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class LogInScenePresentation implements LogInPresentation {
	private final Stage stage;
	private final LogInInterface logic;

	public LogInScenePresentation(Stage stage, LogInInterface logic) {
		this.stage = stage;
		this.logic = logic;
	}

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

	@Override
	public void show() throws WindowLoadFailureException {
		// TODO Auto-generated method stub
		FXMLLoader loader = new FXMLLoader(LogInScenePresentation.class.getResource("LogInGUI.fxml"));
		loader.setController(this);
		Parent parent;
		try {
			parent = loader.load();
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}
		Scene s = new Scene(parent);
		stage.setScene(s);
	}

	@Override
	public void hide() {// Do nothing when done.
	}
}
