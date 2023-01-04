package pala.apps.arlith.frontend.themes.arlithdefault.clientgui;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
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
	public @FXML TextField passwordPromptField;
	// Goes right before "Log In" button in GUI when needed.
	public final Text loginErrorText = new Text();
	{
		loginErrorText.setFill(Color.FIREBRICK);
	}
	public @FXML Button logInButton;

	private @FXML void initialize() {
		EventHandler<ActionEvent> handler = a -> logic.triggerLogIn();
		usernamePromptField.setOnAction(handler);
		passwordPromptField.setOnAction(handler);
		logInButton.setOnAction(handler);
	}

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

	@Override
	public void showLoginProblem(LoginProblemValue problem) {
		switch (problem) {
		case ILLEGAL_EM:
			loginErrorText.setText("That email address is not allowed.");
			break;
		case ILLEGAL_PH:
			loginErrorText.setText("That phone number is not allowed.");
			break;
		case ILLEGAL_PW:
			loginErrorText.setText("That password is not allowed.");
			break;
		case ILLEGAL_UN:
			loginErrorText.setText("That username is not allowed.");
			break;
		case LONG_EM:
			loginErrorText.setText("The email address you entered is too long.");
			break;
		case LONG_PH:
			loginErrorText.setText("The phone number you entered is too long.");
			break;
		case LONG_PW:
			loginErrorText.setText("The password you entered is too long.");
			break;
		case LONG_UN:
			loginErrorText.setText("The username you entered is too long.");
			break;
		case INVALID_EM:
			loginErrorText.setText("No account with that email address found.");
			break;
		case INVALID_PH:
			loginErrorText.setText("No account with that phone number found.");
			break;
		case INVALID_PW:// I need to find out what these mean again.
			loginErrorText.setText("That password is wrong.");
			break;
		case INVALID_UN:
			loginErrorText.setText("No account with that username found.");
			break;
		case SHORT_EM:
			loginErrorText.setText("The email address you entered is too short.");
			break;
		case SHORT_PH:
			loginErrorText.setText("The phone number you entered is too short.");
			break;
		case SHORT_PW:
			loginErrorText.setText("The password you entered is too short.");
			break;
		case SHORT_UN:
			loginErrorText.setText("The username you entered is too short.");
			break;
		default:
			loginErrorText.setText("An unknown error occurred.");
			break;
		}

		if (!logInBox.getChildren().contains(loginErrorText))
			// Add error text right before "Log In" button.
			logInBox.getChildren().add(logInBox.getChildren().size() - 1, loginErrorText);
	}

	@Override
	public void lockUIForLoggingIn() {
		logInBox.setDisable(true);
	}

	@Override
	public void unlockUIForLoggingIn() {
		logInBox.setDisable(false);
	}
}
