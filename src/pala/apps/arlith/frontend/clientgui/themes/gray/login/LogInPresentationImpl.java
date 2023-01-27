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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
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
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class LogInPresentationImpl implements LogInPresentationWithLiveInputResponse {
	private final LogInLogic logic;
	private Scene scene;

	public LogInPresentationImpl(LogInLogic logic) {
		this.logic = logic;
	}

	private final LogInController loginUI = new LogInController();
	private final CreateAccountController createAccountUI = new CreateAccountController();

	public @FXML BorderPane root;
	public @FXML VBox logInBox;
	public @FXML Text title;
	private final Text infoMessage = new Text();
	private final StackPane infoMessageContainer = new StackPane(infoMessage);
	{
		infoMessageContainer.setMinHeight(50);
		infoMessage.setWrappingWidth(300);
		infoMessage.setFont(Font.font(13));
		infoMessage.setTextAlignment(TextAlignment.CENTER);
		loginUI.getPasswordPrompt().getInput().textProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().getInput().textProperty());
		loginUI.getPasswordPrompt().colorProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().colorProperty());
	}
	private final NiceLookingButton logInButton = new NiceLookingButton("Log In"),
			createAccountButton = new NiceLookingButton("Create Account");

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
		logInBox.getChildren().set(2, loginUI.getContainer());
		title.setText("Log In");
		logInButton.setText("Log In");
		hideInformationMessage();
	}

	private void showCreateAccountUI() {
		logInBox.getChildren().set(2, createAccountUI.getContainer());
		title.setText("Create Account");
		logInButton.setText("Create Account");
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
		logInBox.getChildren().addAll(infoMessageContainer, loginUI.getContainer(), logInButton);
		loginUI.getContainer().setPadding(new Insets(0, 0, 110, 0));// Used to make window large enough to fit
																	// createAccountUI by default.

		// Attach triggers for logic.
		loginUI.getLogInIdentifierPrompt().getInput().textProperty()
				.addListener(a -> logic.triggerCheckLogInIdentifier());
		loginUI.getPasswordPrompt().getInput().textProperty().addListener(a -> logic.triggerCheckPassword());
		createAccountUI.getUsernamePrompt().getInput().textProperty().addListener(a -> logic.triggerCheckUsername());
		createAccountUI.getEmailPrompt().getInput().textProperty().addListener(a -> logic.triggerCheckEmail());
		createAccountUI.getPhoneNumberPrompt().getInput().textProperty()
				.addListener(a -> logic.triggerCheckPhoneNumber());

		loginUI.getCreateAccountHyperlink().setOnAction(a -> showCreateAccountUI());
		createAccountUI.getBackToLogInHyperlink().setOnAction(a -> showLogInUI());

		EventHandler<ActionEvent> logInSubmitHandler = a -> attemptToLogIn(),
				createAccountSubmitHandler = a -> attemptToCreateAccount();
		loginUI.getLogInIdentifierPrompt().getInput().setOnAction(logInSubmitHandler);
		logInButton.setOnAction(logInSubmitHandler);

		createAccountButton.setOnAction(createAccountSubmitHandler);
		createAccountUI.getUsernamePrompt().getInput().setOnAction(createAccountSubmitHandler);
		createAccountUI.getEmailPrompt().getInput().setOnAction(createAccountSubmitHandler);
		createAccountUI.getPhoneNumberPrompt().getInput().setOnAction(createAccountSubmitHandler);

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
		return loginUI.getLogInIdentifierPrompt().getInput().getText();
	}

	@Override
	public String getPassword() {
		return loginUI.getPasswordPrompt().getInput().getText();
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
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Illegal Email");
			break;
		case ILLEGAL_PH:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Illegal Phone #");
			break;
		case ILLEGAL_PW:
			(prompt = loginUI.getPasswordPrompt()).getInformationText().setText("Illegal Password");
			break;
		case ILLEGAL_UN:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Illegal Tag");
			break;
		case INVALID_EM:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Invalid Email");
			break;
		case INVALID_PH:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Invalid Phone #");
			break;
		case INVALID_PW:
			(prompt = loginUI.getPasswordPrompt()).getInformationText().setText("Invalid Password");
			break;
		case INVALID_UN:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Invalid Username");
			break;
		case LONG_EM:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Email too long");
			break;
		case LONG_PH:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Phone # too long");
			break;
		case LONG_PW:
			(prompt = loginUI.getPasswordPrompt()).getInformationText().setText("Password too long");
			break;
		case LONG_UN:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Username too long");
			break;
		case SHORT_EM:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Email too short");
			break;
		case SHORT_PH:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Phone # too short");
			break;
		case SHORT_PW:
			(prompt = loginUI.getPasswordPrompt()).getInformationText().setText("Password too short");
			break;
		case SHORT_UN:
			(prompt = loginUI.getLogInIdentifierPrompt()).getInformationText().setText("Username too short");
			break;
		default:
			return;
		}
		prompt.showInformation();
		prompt.setHue(0);
		prompt.setSaturation(1);
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
		return createAccountUI.getEmailPrompt().getInput().getText();
	}

	@Override
	public String getPhoneNumber() {
		return createAccountUI.getPhoneNumberPrompt().getInput().getText();
	}

	@Override
	public String getUsername() {
		return createAccountUI.getUsernamePrompt().getInput().getText();
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
		if (issue == null)
			if (getPhoneNumber().isEmpty())
				createAccountUI.getPhoneNumberPrompt().resetColor();
			else
				createAccountUI.getPhoneNumberPrompt().colorTextBox(120);
		else if (issue.getSeverity() == Severity.ERROR) {
			createAccountUI.getPhoneNumberPrompt().colorTextBox(0);
			// As long as there is an error, disable the input.
			return;
		} else if (issue.getSeverity() == Severity.WARNING)
			createAccountUI.getPhoneNumberPrompt().colorTextBox(60);
	}

}
