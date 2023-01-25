package pala.apps.arlith.frontend.clientgui.themes.gray.login;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class LogInPresentationImpl implements LogInPresentationWithLiveInputResponse {
	private final LogInLogic logic;
	private Scene scene;

	public LogInPresentationImpl(LogInLogic logic) {
		this.logic = logic;
	}

	public @FXML BorderPane root;
	public @FXML VBox logInBox, inputsBox;
	public @FXML Text title;
	private final Text infoMessage = new Text();
	{
		infoMessage.setWrappingWidth(300);
		infoMessage.setFont(Font.font(13));
		infoMessage.setTextAlignment(TextAlignment.CENTER);
	}
	private final SilverTextBox logInIdentPrompt = new SilverTextBox(), passwordPrompt = new SilverTextBox(true),
			usernamePrompt = new SilverTextBox(), emailPrompt = new SilverTextBox(),
			phoneNumberPrompt = new SilverTextBox();
	private final NiceLookingButton logInButton = new NiceLookingButton("Log In"),
			createAccountButton = new NiceLookingButton("Create Account");

	private final Hyperlink createAccountHyperlink = new StyledHyperlink("Create Account..."),
			backToLogInHyperlink = new StyledHyperlink("Back to Log In");

	private void informUserOfError(String text) {
		infoMessage.setText(text);
		infoMessage.setFill(Color.hsb(0, 0.9, 0.6));
	}

	private void informUser(String text) {
		infoMessage.setText(text);
		infoMessage.setFill(Color.hsb(240, 0.7, 1));
	}

	private void hideInformationMessage() {
		infoMessage.setText("");
	}

	private void attemptToLogIn() {
		informUser("Attempting to log in...");
		logic.triggerLogIn();
	}

	private void attemptToCreateAccount() {
		informUser("Attempting to create an account...");
		logic.triggerCreateAccount();
	}

	private void showLogInUI() {
		inputsBox.getChildren().setAll(logInIdentPrompt, passwordPrompt, logInButton);
		logInIdentPrompt.getPrompt().setText("Account Tag/Email/Phone");
		inputsBox.setSpacing(40);
		if (passwordPrompt.getChildren().size() == 3)
			passwordPrompt.getChildren().add(3, createAccountHyperlink);
		else
			passwordPrompt.getChildren().set(3, createAccountHyperlink);
		passwordPrompt.getInput().setOnAction(a -> logic.triggerLogIn());
		hideInformationMessage();
	}

	private void showCreateAccountUI() {
		inputsBox.getChildren().setAll(usernamePrompt, emailPrompt, phoneNumberPrompt, passwordPrompt,
				createAccountButton);
		inputsBox.setSpacing(30);
		if (passwordPrompt.getChildren().size() == 3)
			passwordPrompt.getChildren().add(3, backToLogInHyperlink);
		else
			passwordPrompt.getChildren().set(3, backToLogInHyperlink);
		passwordPrompt.getInput().setOnAction(a -> logic.triggerCreateAccount());
		hideInformationMessage();
	}

	private @FXML void initialize() {
		root.setOnMouseClicked(a -> root.requestFocus());
		root.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(new RadialGradient(30, .2, .3, -.1, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, Color.DARKGRAY), new Stop(.4, Color.TRANSPARENT)), CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(new RadialGradient(0, 0, 1, .7, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, Color.DARKGRAY), new Stop(.4, Color.TRANSPARENT)), CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(
						new RadialGradient(-30, -.1, .2, .9, 1, true, CycleMethod.NO_CYCLE,
								new Stop(0, Color.color(.58, .58, .58)), new Stop(.4, Color.TRANSPARENT)),
						CornerRadii.EMPTY, Insets.EMPTY)));

		showLogInUI();
		logInBox.getChildren().add(1, infoMessage);

		logInIdentPrompt.setNecessary(true);
		passwordPrompt.getPrompt().setText("Password");
		passwordPrompt.setNecessary(true);
		logInIdentPrompt.setPrefWidth(300);
		passwordPrompt.setPrefWidth(300);

		usernamePrompt.setPrefWidth(300);
		emailPrompt.setPrefWidth(300);
		phoneNumberPrompt.setPrefWidth(300);
		usernamePrompt.getPrompt().setText("Username");
		usernamePrompt.setNecessary(true);
		emailPrompt.getPrompt().setText("Email");
		emailPrompt.setNecessary(true);
		phoneNumberPrompt.getPrompt().setText("Phone Number");

		// Attach triggers for logic.
		logInIdentPrompt.getInput().textProperty().addListener(a -> logic.triggerCheckLogInIdentifier());
		passwordPrompt.getInput().textProperty().addListener(a -> logic.triggerCheckPassword());
		usernamePrompt.getInput().textProperty().addListener(a -> logic.triggerCheckUsername());
		emailPrompt.getInput().textProperty().addListener(a -> logic.triggerCheckEmail());
		phoneNumberPrompt.getInput().textProperty().addListener(a -> logic.triggerCheckPhoneNumber());

		createAccountHyperlink.setOnAction(a -> showCreateAccountUI());
		backToLogInHyperlink.setOnAction(a -> showLogInUI());

		EventHandler<ActionEvent> logInSubmitHandler = a -> attemptToLogIn(),
				createAccountSubmitHandler = a -> attemptToCreateAccount();
		logInIdentPrompt.getInput().setOnAction(logInSubmitHandler);
		logInButton.setOnAction(logInSubmitHandler);

		createAccountButton.setOnAction(createAccountSubmitHandler);
		usernamePrompt.getInput().setOnAction(createAccountSubmitHandler);
		emailPrompt.getInput().setOnAction(createAccountSubmitHandler);
		phoneNumberPrompt.getInput().setOnAction(createAccountSubmitHandler);

		logInButton.setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		logInButton.hoverProperty().addListener((observable, oldValue, newValue) -> {
			if (!logInButton.isPressed())
				if (newValue)
					logInButton.setBackground(
							FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate().desaturate()));
				else
					logInButton
							.setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		});
		logInButton.armedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
				logInButton.setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE));
			else if (logInButton.isHover())
				logInButton.setBackground(
						FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate().desaturate()));
			else
				logInButton.setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		});
	}

	@Override
	public Scene getScene() throws WindowLoadFailureException {
		if (scene != null)
			return scene;
		FXMLLoader loader = new FXMLLoader(LogInPresentationImpl.class.getResource("LogInGUI.fxml"));
		loader.setController(this);
		Parent parent;
		try {
			parent = loader.load();
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}
		return scene = new Scene(parent);
	}

	@Override
	public String getLogInIdentifier() {
		return logInIdentPrompt.getInput().getText();
	}

	@Override
	public String getPassword() {
		return logInIdentPrompt.getInput().getText();
	}

	@Override
	public void showLoginProblem(LoginProblemValue problem) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> showLoginProblem(problem));
			return;
		}
		informUserOfError("The server rejected your log in information.");
		SilverTextBox prompt;
		switch (problem) {
		case ILLEGAL_EM:
			(prompt = logInIdentPrompt).getInformationText().setText("Illegal Email");
			break;
		case ILLEGAL_PH:
			(prompt = logInIdentPrompt).getInformationText().setText("Illegal Phone #");
			break;
		case ILLEGAL_PW:
			(prompt = passwordPrompt).getInformationText().setText("Illegal Password");
			break;
		case ILLEGAL_UN:
			(prompt = logInIdentPrompt).getInformationText().setText("Illegal Tag");
			break;
		case INVALID_EM:
			(prompt = logInIdentPrompt).getInformationText().setText("Invalid Email");
			break;
		case INVALID_PH:
			(prompt = logInIdentPrompt).getInformationText().setText("Invalid Phone #");
			break;
		case INVALID_PW:
			(prompt = passwordPrompt).getInformationText().setText("Invalid Password");
			break;
		case INVALID_UN:
			(prompt = logInIdentPrompt).getInformationText().setText("Invalid Username");
			break;
		case LONG_EM:
			(prompt = logInIdentPrompt).getInformationText().setText("Email too long");
			break;
		case LONG_PH:
			(prompt = logInIdentPrompt).getInformationText().setText("Phone # too long");
			break;
		case LONG_PW:
			(prompt = passwordPrompt).getInformationText().setText("Password too long");
			break;
		case LONG_UN:
			(prompt = logInIdentPrompt).getInformationText().setText("Username too long");
			break;
		case SHORT_EM:
			(prompt = logInIdentPrompt).getInformationText().setText("Email too short");
			break;
		case SHORT_PH:
			(prompt = logInIdentPrompt).getInformationText().setText("Phone # too short");
			break;
		case SHORT_PW:
			(prompt = passwordPrompt).getInformationText().setText("Password too short");
			break;
		case SHORT_UN:
			(prompt = logInIdentPrompt).getInformationText().setText("Username too short");
			break;
		default:
			return;
		}
		prompt.showInformation();
		prompt.setColor(Color.FIREBRICK);
	}

	@Override
	public void lockUIForLoggingIn() {
		logInBox.setDisable(true);
	}

	@Override
	public void unlockUIForLoggingIn() {
		logInBox.setDisable(false);
	}

	@Override
	public String getEmail() {
		return emailPrompt.getInput().getText();
	}

	@Override
	public String getPhoneNumber() {
		return phoneNumberPrompt.getInput().getText();
	}

	@Override
	public String getUsername() {
		return usernamePrompt.getInput().getText();
	}

	@Override
	public void showLogInFailure(LoginFailureException error) {
		informUserOfError(
				"Failed to connect to (or negotiate with) server. This is usually because the server is offline or there's no internet. (See the log or speak to someone for details.)");
	}

	@Override
	public void showLogInIdentifierError(Issue issue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showUsernameError(Issue issue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showPasswordError(Issue issue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showEmailError(Issue issue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showPhoneNumberError(Issue issue) {
		// TODO Auto-generated method stub
		if (issue == null) {
			if (getPhoneNumber().isEmpty()) {

			} else {
				phoneNumberPrompt.setColor(Color.GREEN);
			}
		}

	}

}
