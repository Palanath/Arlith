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
import pala.libs.generic.JavaTools;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class LogInPresentationImpl implements LogInPresentationWithLiveInputResponse {
	private final LogInLogic logic;
	private Scene scene;

	private final LogInController loginUI = new LogInController();
	private final CreateAccountController createAccountUI = new CreateAccountController();

	private @FXML BorderPane root;

	private @FXML VBox logInBox;
	private @FXML Text title;
	private final Text infoMessage = new Text();
	private final StackPane infoMessageContainer = new StackPane(infoMessage);
	{
		infoMessageContainer.setMinHeight(50);
		infoMessage.setWrappingWidth(300);
		infoMessage.setFont(Font.font(13));
		infoMessage.setTextAlignment(TextAlignment.CENTER);

		// Bind password inputs' (1) text content, (2) showInfo property (whether info
		// string is visible or not), (3) information content, (4) color.
		//
		// The main difference between the two prompts is that the loginUI is the only
		// prompt that stores "validity" by the #setState(SilverTextBox, Severity) and
		// checkValid(SilverTextBox) methods.
		loginUI.getPasswordPrompt().getInput().textProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().getInput().textProperty());
		loginUI.getPasswordPrompt().getInformationText().textProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().getInformationText().textProperty());
		loginUI.getPasswordPrompt().showInformationProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().showInformationProperty());
		loginUI.getPasswordPrompt().colorProperty()
				.bindBidirectional(createAccountUI.getPasswordPrompt().colorProperty());
		// The two password prompts are bound but still kept separate nodes because (1)
		// if there were only one node, it would have to be swapped around between the
		// two UI parts (that is problematic with where it is in the scene graph) and
		// (2) when enter is pressed and they are focused, different actions take place
		// (log in for log in UI's password prompt, and account creation for create
		// account UI's password prompt).
	}
	private final NiceLookingButton logInButton = new NiceLookingButton("Log In"),
			createAccountButton = new NiceLookingButton("Create Account");

	public LogInPresentationImpl(final LogInLogic logic) {
		this.logic = logic;
	}

	private void attemptToCreateAccount() {
		if (createAccountButton.isDisable())
			return;// Don't execute if inputs not valid.
		informUser("Attempting to create an account...");
		logic.triggerCreateAccount();
	}

	private void attemptToLogIn() {
		if (logInButton.isDisable())
			return;// Don't execute if inputs not valid.
		informUser("Attempting to log in...");
		logic.triggerLogIn();
	}

	private void calcCreateAccountDisabled() {
		createAccountButton.setDisable(!checkValid(createAccountUI.getEmailPrompt())
				|| !checkValid(createAccountUI.getPhoneNumberPrompt())
				|| !checkValid(createAccountUI.getUsernamePrompt()) || !checkValid(loginUI.getPasswordPrompt()));
	}

	/**
	 * Determines whether all of the necessary inputs for logging in have
	 * syntactically correct input (using {@link #checkValid(SilverTextBox)}) and if
	 * the unnecessary inputs that have values are syntactically correct, and
	 * updates the {@link #loginUI}'s {@link LogInController#}
	 */
	private void calcLogInDisabled() {
		logInButton.setDisable(
				!checkValid(loginUI.getLogInIdentifierPrompt()) || !checkValid(loginUI.getPasswordPrompt()));
	}

	/**
	 * <p>
	 * Determines whether the content of a {@link SilverTextBox} is valid or not by
	 * querying its <code>valid</code> property. The <code>valid</code> property is
	 * stored in the {@link SilverTextBox#getProperties()} map, and is meant to be
	 * accessed and set only by {@link #checkValid(SilverTextBox)} and
	 * {@link #setValid(SilverTextBox)}, respectively.
	 * </p>
	 * <p>
	 * The validity of one of the {@link SilverTextBox} inputs tracked by this
	 * {@link LogInPresentationImpl} determines whether the user data entered in the
	 * prompt is valid. This check is only made once every time the user changes the
	 * data they've entered in an input, so that live user feedback may be given.
	 * </p>
	 * <p>
	 * If the input value is syntactically invalid (with {@link Severity#ERROR}),
	 * the {@link LogInPresentationImpl} should not be submitable. To facilitate
	 * implementing this UI requirement, this class keeps track of each
	 * {@link SilverTextBox} that currently has valid user input in it, and
	 * recalculates whether the submit buttons should be disabled whenever a user
	 * input is updated.
	 * </p>
	 * <p>
	 * By default, all {@link SilverTextBox}es that are
	 * {@link SilverTextBox#isNecessary() necessary} are set to invalid (since they
	 * begin empty). Apart from this, setting the validity of the input boxes is up
	 * to the presentation class.
	 * </p>
	 *
	 * @param box The input box to check the validity of.
	 * @return <code>true</code> if valid, <code>false</code> otherwise.
	 */
	private boolean checkValid(final SilverTextBox box) {
		return !box.getProperties().containsKey(LogInPresentationImpl.class);
	}

	private SilverTextBox getInput(Input input) {
		switch (input) {
		case EMAIL_ADDRESS:
			return createAccountUI.getEmailPrompt();
		case LOGIN_IDENTIFIER:
			return loginUI.getLogInIdentifierPrompt();
		case PASSWORD:
			return loginUI.getPasswordPrompt();
		case PHONE_NUMBER:
			return createAccountUI.getPhoneNumberPrompt();
		case USERNAME:
			return createAccountUI.getUsernamePrompt();
		default:
			throw new IllegalArgumentException("Unknown input type");
		}
	}

	@Override
	public String getInputValue(Input input) {
		return getInput(input).getInput().getText();
	}

	@Override
	public Scene getScene() throws WindowLoadFailureException {
		if (scene != null)
			return scene;
		final FXMLLoader loader = new FXMLLoader(LogInPresentationImpl.class.getResource("LogInGUI.fxml"));
		loader.setController(this);
		Parent parent;
		try {
			parent = loader.load();
		} catch (final IOException e) {
			throw new WindowLoadFailureException(e);
		}
		return scene = new Scene(parent);
	}

	private void hideInformationMessage() {
		infoMessage.setText("");
	}

	private void informUser(final String text) {
		infoMessage.setText(text);
		infoMessage.setFill(Color.hsb(240, 0.7, 1));
	}

	private void informUserOfError(final String text) {
		infoMessage.setText(text);
		infoMessage.setFill(Color.hsb(0, 0.9, 0.6));
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

		final EventHandler<ActionEvent> logInSubmitHandler = a -> attemptToLogIn(),
				createAccountSubmitHandler = a -> attemptToCreateAccount();
		loginUI.getLogInIdentifierPrompt().getInput().setOnAction(logInSubmitHandler);
		loginUI.getPasswordPrompt().getInput().setOnAction(logInSubmitHandler);
		logInButton.setOnAction(logInSubmitHandler);

		createAccountButton.setOnAction(createAccountSubmitHandler);
		createAccountUI.getUsernamePrompt().getInput().setOnAction(createAccountSubmitHandler);
		createAccountUI.getEmailPrompt().getInput().setOnAction(createAccountSubmitHandler);
		createAccountUI.getPhoneNumberPrompt().getInput().setOnAction(createAccountSubmitHandler);
		createAccountUI.getPasswordPrompt().getInput().setOnAction(createAccountSubmitHandler);

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

		// By default, every prompt is in the valid state. Over here, we invalidate the
		// ones that are necessary.

		loginUI.getLogInIdentifierPrompt().getProperties().put(LogInPresentationImpl.class, null);
		loginUI.getPasswordPrompt().getProperties().put(LogInPresentationImpl.class, null);
		createAccountUI.getEmailPrompt().getProperties().put(LogInPresentationImpl.class, null);
		createAccountUI.getUsernamePrompt().getProperties().put(LogInPresentationImpl.class, null);
		calcLogInDisabled();// Disable inputs accordingly.
		calcCreateAccountDisabled();
	}

	@Override
	public void lockUIForLoggingIn() {
		logInBox.setDisable(true);
	}

	/**
	 * <p>
	 * Sets the validity and color of the specified {@link SilverTextBox}. The color
	 * is derived from the validity, so this method sets both. If the input is valid
	 * (i.e. <code>severity</code> is <code>null</code>), this method also checks to
	 * see if it is empty. If the input box is both valid and empty, its color is
	 * reset, rather than set to {@link Color#GREEN}.
	 * </p>
	 *
	 * @param box      The input box.
	 * @param severity The {@link Severity} of the issue that occurred (used to
	 *                 determine the validity of the input box and its color), or
	 *                 <code>null</code> if there was no issue.
	 */
	private void setState(final SilverTextBox box, final Severity severity) {
		boolean swapping = box.getProperties().containsKey(LogInPresentationImpl.class) ^ severity == Severity.ERROR;
		if (severity == Severity.ERROR) {// Issue preventing data submission.
			box.getProperties().put(LogInPresentationImpl.class, null);
			box.setColor(Color.RED);
		} else {// No issue or WARNING; the data can be submit.
			box.getProperties().remove(LogInPresentationImpl.class);
			box.setColor(severity == Severity.WARNING ? Color.color(.8, .7, 0, 1)
					: box.getInput().getText().isEmpty() ? null : Color.hsb(120, .6, .93));
		}
		if (swapping) {
			// Recalculate buttons' disabled-ness.
			calcCreateAccountDisabled();
			calcLogInDisabled();
		}
	}

	private void showCreateAccountUI() {
		JavaTools.swap(logInBox.getChildren(), loginUI.getContainer(), createAccountUI.getContainer());
		title.setText("Create Account");
		JavaTools.swap(logInBox.getChildren(), logInButton, createAccountButton);
		hideInformationMessage();
	}

	@Override
	public void showInputError(Issue issue, Input input) {
		SilverTextBox box = getInput(input);
		setState(box, issue.getSeverity());
		box.showInformation(issue.message());
	}

	@Override
	public void showLogInFailure(final LoginFailureException error) {
		informUserOfError(
				"Failed to connect to (or negotiate with) server. This is usually because the server is offline or there's no internet. (See the log or speak to someone for details.)");
	}

	@Override
	public void showLoginProblem(final LoginProblemValue problem) {
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

	private void showLogInUI() {
		JavaTools.swap(logInBox.getChildren(), createAccountUI.getContainer(), loginUI.getContainer());
		title.setText("Log In");
		JavaTools.swap(logInBox.getChildren(), createAccountButton, logInButton);
		hideInformationMessage();
	}

	@Override
	public void showInputValid(Input input) {
		SilverTextBox box = getInput(input);
		setState(box, null);
		box.hideInformation();
	}

	@Override
	public void unlockUIForLoggingIn() {
		logInBox.setDisable(false);
	}

	@Override
	public void showLogInError(String error) {
		infoMessage.setText(error);
		infoMessage.setFill(Color.hsb(0, 0.7, 0.5));
	}

}
