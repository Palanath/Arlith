package pala.apps.arlith.frontend.guis.login;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.ArlithRuntime.Instance;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.guis.ApplicationState;
import pala.apps.arlith.frontend.guis.home.HomePage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.Utilities.UserReference;
import pala.libs.generic.guis.ApplicationProperties;
import pala.libs.generic.guis.Window;

public final class LogInWindow extends Window {

	public static void dbg(String txt) {
		ArlithFrontend.getGuiLogger().dbg("[CLIENT]: " + txt);
	}

	private AnchorPane root;

	private final Canvas canvas = new Canvas();
	private final LogInBackground bg = new LogInBackground(canvas);
	private @FXML TextField createAccountUsernamePrompt, createAccountPasswordPrompt, createAccountEmailPrompt,
			createAccountPhonePrompt, logInIdentifierPrompt, logInPasswordPrompt;
	private @FXML VBox box;
	private @FXML StackPane centerPanel, loginPanel, createAccountPanel;
	private @FXML Text motd, createAccountUsernameError, createAccountEmailError, createAccountPhoneError,
			createAccountPasswordError, loginPasswordError, loginIdentifierError;

	private final Stage launchSettingsStage = new Stage();
	private final LaunchSettingsWindow launchSettingsWindow = new LaunchSettingsWindow();
	{
		try {
			launchSettingsWindow.display(launchSettingsStage);
		} catch (WindowLoadFailureException e) {
			ArlithFrontend.getGuiLogger().err(e);
		}
	}

	private final Object PROMPT_COMPLETE_KEY = new Object();

	private void error(TextField prompt, Text errorTxt, String error) {
		// For when things are errors.
		prompt.setEffect(new DropShadow(10, Color.FIREBRICK));
		errorTxt.setText(error);
		errorTxt.setStyle("-fx-fill:firebrick;");
		errorTxt.setVisible(true);
		prompt.getProperties().put(PROMPT_COMPLETE_KEY, false);
	}

	private void warn(TextField prompt, Text errorTxt, String txt) {
		prompt.setEffect(new DropShadow(10, Color.GOLD));
		errorTxt.setText(txt);
		errorTxt.setStyle("-fx-fill:gold;");
		errorTxt.setVisible(true);
		prompt.getProperties().put(PROMPT_COMPLETE_KEY, false);
	}

	private void valid(TextField prompt, Text errorTxt, String txt) {
		// For when things are valid.
		prompt.setEffect(new DropShadow(10, Color.GREEN));
		errorTxt.setText(txt);
		errorTxt.setStyle("-fx-fill:green;");
		errorTxt.setVisible(true);
		prompt.getProperties().put(PROMPT_COMPLETE_KEY, true);
	}

	private void clearError(TextField prompt, Text errorTxt) {
		errorTxt.setText("");
		errorTxt.setVisible(false);
		prompt.setStyle("");
		prompt.setEffect(null);
		prompt.getProperties().remove(PROMPT_COMPLETE_KEY);
	}

	private @FXML void initialize() {
		createAccountUsernamePrompt.textProperty().bindBidirectional(logInIdentifierPrompt.textProperty());
		createAccountPasswordPrompt.textProperty().bindBidirectional(logInPasswordPrompt.textProperty());

		logInIdentifierPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
				clearError(logInIdentifierPrompt, loginIdentifierError);
			else if (Utilities.isValidUsernameReference(newValue) != null || Utilities.isValidEmail(newValue)
					|| Utilities.isValidPhoneNumber(newValue))
				valid(logInIdentifierPrompt, loginIdentifierError, "\u2713");
			else
				error(logInIdentifierPrompt, loginIdentifierError, "Invalid account.");
		});
		logInPasswordPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
				clearError(logInPasswordPrompt, loginPasswordError);
			else
				valid(logInPasswordPrompt, loginPasswordError, "\u2713");
		});

		createAccountUsernamePrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.contains("#"))
				error(createAccountUsernamePrompt, createAccountUsernameError, "'#' is a reserved character.");
			else if (newValue.contains("<") || newValue.contains(">"))
				error(createAccountUsernamePrompt, createAccountUsernameError, "'<' and '>' are reserved characters.");
			else if (newValue.contains("@"))
				error(createAccountUsernamePrompt, createAccountUsernameError, "'@' is a reserved character.");
			else if (newValue.length() > 20)
				error(createAccountUsernamePrompt, createAccountUsernameError, "Username too long!");
			else if (newValue.length() < 3)
				if (newValue.isEmpty())
					clearError(createAccountUsernamePrompt, createAccountUsernameError);
				else
					error(createAccountUsernamePrompt, createAccountUsernameError, "Username too short!");
			else
				valid(createAccountUsernamePrompt, createAccountUsernameError, "\u2713");
		});
		createAccountEmailPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.length() > 321)
				error(createAccountEmailPrompt, createAccountEmailError, "Email too long!");
			else if (newValue.length() < 3)
				if (newValue.isEmpty())
					clearError(createAccountEmailPrompt, createAccountEmailError);
				else
					error(createAccountEmailPrompt, createAccountEmailError, "Email too short!");
			else if (!Utilities.isValidEmail(newValue))
				error(createAccountEmailPrompt, createAccountEmailError, "Invalid email!");
			else
				valid(createAccountEmailPrompt, createAccountEmailError, "\u2713");
		});
		createAccountPhonePrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
				clearError(createAccountPhonePrompt, createAccountPhoneError);
			else if (newValue.length() < 10)
				error(createAccountPhonePrompt, createAccountPhoneError, "Phone # too short!");
			else if (!Utilities.isValidPhoneNumber(newValue))
				error(createAccountPhonePrompt, createAccountPhoneError, "Invalid phone #.");
			else
				valid(createAccountPhonePrompt, createAccountPhoneError, "\u2713");

		});
		createAccountPasswordPrompt.textProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
				clearError(createAccountPasswordPrompt, createAccountPasswordError);
			else
				valid(createAccountPasswordPrompt, createAccountPasswordError, "\u2713");
		});

	}

	private Stage stage;
	private boolean acting;

	private @FXML void showCreateAccount() {
		createAccountPanel.setVisible(true);
		loginPanel.setVisible(false);
	}

	private @FXML void showLogIn() {
		loginPanel.setVisible(true);
		createAccountPanel.setVisible(false);
	}

	private boolean complete(Node node) {
		return node.getProperties().containsKey(PROMPT_COMPLETE_KEY)
				&& (Boolean) node.getProperties().get(PROMPT_COMPLETE_KEY) == true;
	}

	private synchronized @FXML void onCreateAccount() {
		if (acting)
			return;
		acting = true;
		centerPanel.setDisable(true);

		if (createAccountEmailPrompt.getText().isEmpty()) {
			warn(createAccountEmailPrompt, createAccountEmailError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			if (createAccountUsernamePrompt.getText().isEmpty())
				warn(createAccountUsernamePrompt, createAccountUsernameError, "This can't be empty!");
			if (createAccountPasswordPrompt.getText().isEmpty())
				warn(createAccountPasswordPrompt, createAccountPasswordError, "This can't be empty!");
			if (createAccountEmailPrompt.getText().isEmpty())
				warn(createAccountEmailPrompt, createAccountEmailError, "This can't be empty!");
			return;
		} else if (createAccountUsernamePrompt.getText().isEmpty()) {
			warn(createAccountUsernamePrompt, createAccountUsernameError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			if (createAccountPasswordPrompt.getText().isEmpty())
				warn(createAccountPasswordPrompt, createAccountPasswordError, "This can't be empty!");
			if (createAccountEmailPrompt.getText().isEmpty())
				warn(createAccountEmailPrompt, createAccountEmailError, "This can't be empty!");
			return;
		} else if (createAccountPasswordPrompt.getText().isEmpty()) {
			warn(createAccountPasswordPrompt, createAccountPasswordError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			if (createAccountEmailPrompt.getText().isEmpty())
				warn(createAccountEmailPrompt, createAccountEmailError, "This can't be empty!");
			return;
		} else if (createAccountEmailPrompt.getText().isEmpty()) {
			warn(createAccountEmailPrompt, createAccountEmailError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			return;
		}

		if (!(complete(createAccountUsernamePrompt) && complete(createAccountEmailPrompt)
				&& complete(createAccountPasswordPrompt))) {
			acting = false;
			centerPanel.setDisable(false);
			return;
		}

		String un = createAccountUsernamePrompt.getText(), pw = createAccountPasswordPrompt.getText(),
				em = createAccountEmailPrompt.getText(), pn = createAccountPhonePrompt.getText();

		Thread connectionThread = ArlithRuntime.newThread(Instance.CLIENT, () -> {
			ArlithClient client;
			try {
				client = new ArlithClientBuilder(un, pw, Utilities.getPreferredDestinationAddress()).setEmail(em)
						.setPhoneNumber(pn).createAccount();
			} catch (CreateAccountError e3) {
				Platform.runLater(() -> {
					ArlithFrontend.getGuiLogger()
							.err("Failed to create account; server returned failure code: " + e3.getType() + '.');
					switch (e3.getType()) {
					case ILLEGAL_PW:
						ArlithFrontend.getGuiLogger().err("Illegal Password");
						break;
					case ILLEGAL_UN:
						ArlithFrontend.getGuiLogger().err("Illegal Username");
						break;
					case LONG_PW:
						ArlithFrontend.getGuiLogger().err("Password too long");
						break;
					case LONG_UN:
						ArlithFrontend.getGuiLogger().err("Username too long");
						break;
					case SHORT_PW:
						ArlithFrontend.getGuiLogger().err("Password too short");
						break;
					case SHORT_UN:
						ArlithFrontend.getGuiLogger().err("Username too short");
						break;
					case ILLEGAL_EM:
						ArlithFrontend.getGuiLogger().err("Illegal Email");
						break;
					case LONG_EM:
						ArlithFrontend.getGuiLogger().err("Email too long");
						break;
					case TAKEN_EM:
						ArlithFrontend.getGuiLogger().err("Email already in use");
						break;
					case TAKEN_UN:
						ArlithFrontend.getGuiLogger().err("Username taken");
						break;
					case ILLEGAL_PH:
						ArlithFrontend.getGuiLogger().err("Illegal phone number");
						break;
					case LONG_PH:
						ArlithFrontend.getGuiLogger().err("Phone number too long");
						break;
					case SHORT_PH:
						ArlithFrontend.getGuiLogger().err("Phone number too short");
						break;
					case TAKEN_PH:
						ArlithFrontend.getGuiLogger().err("Phone number already in use");
						break;
					}
				});
				acting = false;
				return;
			} catch (Exception e4) {
				ArlithFrontend.getGuiLogger().err(e4);
				acting = false;
				return;
			} finally {
				centerPanel.setDisable(false);
			}

			Platform.runLater(() -> {
				try {
					(ArlithRuntime.window = new ArlithWindow(new ApplicationState(stage, client))).show(new HomePage())
							.display(stage);
				} catch (Exception e2) {
					client.stop();
					ArlithFrontend.getGuiLogger().err(e2);
					acting = false;
				}
			});
		});
		connectionThread.setDaemon(true);
		connectionThread.start();
	}

	private synchronized @FXML void onLogIn() {
		if (acting)
			return;
		acting = true;
		centerPanel.setDisable(true);

		if (logInIdentifierPrompt.getText().isEmpty()) {
			warn(logInIdentifierPrompt, loginIdentifierError, "This can't be empty!");
			if (logInPasswordPrompt.getText().isEmpty())
				warn(logInPasswordPrompt, loginPasswordError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			return;
		} else if (logInPasswordPrompt.getText().isEmpty()) {
			warn(logInPasswordPrompt, loginPasswordError, "This can't be empty!");
			acting = false;
			centerPanel.setDisable(false);
			return;
		}

		if (!(complete(logInIdentifierPrompt) && complete(logInPasswordPrompt))) {
			acting = false;
			centerPanel.setDisable(false);
			return;
		}
		ArlithClientBuilder builder;
		try {
			UserReference ur;
			if (Utilities.isValidEmail(logInIdentifierPrompt.getText())) {
				builder = new ArlithClientBuilder();
				builder.setEmail(logInIdentifierPrompt.getText());
				builder.setPassword(logInPasswordPrompt.getText());
				builder.setHost(InetAddress.getByName(Utilities.getPreferredDestinationAddress()));
			} else if ((ur = Utilities.isValidUsernameReference(logInIdentifierPrompt.getText())) != null)
				builder = new ArlithClientBuilder(ur.getUsername(), ur.getDisc(), logInPasswordPrompt.getText(),
						"localhost");
			else {
				builder = new ArlithClientBuilder();
				builder.setPhoneNumber(logInIdentifierPrompt.getText());
				builder.setPassword(logInPasswordPrompt.getText());
				builder.setHost(InetAddress.getByName(Utilities.getPreferredDestinationAddress()));
			}
		} catch (Exception e) {
			ArlithFrontend.getGuiLogger().err(e);
			acting = false;
			centerPanel.setDisable(false);
			return;
		}

		Thread connectionThread = ArlithRuntime.newThread(Instance.CLIENT, () -> {
			ArlithClient client;
			try {

				client = builder.login();
			} catch (LoginError e3) {
				Platform.runLater(() -> {
					ArlithFrontend.getGuiLogger()
							.err("Failed to log in; server returned failure code: " + e3.getLoginError() + '.');
					switch (e3.getLoginError()) {
					case ILLEGAL_PW:
						ArlithFrontend.getGuiLogger().err("Illegal Password");
						break;
					case ILLEGAL_UN:
						ArlithFrontend.getGuiLogger().err("Illegal Username");
						break;
					case INVALID_PW:
						ArlithFrontend.getGuiLogger().err("Invalid Password");
						break;
					case INVALID_UN:
						ArlithFrontend.getGuiLogger().err("Invalid Username");
						break;
					case LONG_PW:
						ArlithFrontend.getGuiLogger().err("Password too long");
						break;
					case LONG_UN:
						ArlithFrontend.getGuiLogger().err("Username too long");
						break;
					case SHORT_PW:
						ArlithFrontend.getGuiLogger().err("Password too short");
						break;
					case SHORT_UN:
						ArlithFrontend.getGuiLogger().err("Username too short");
						break;
					case ILLEGAL_EM:
						ArlithFrontend.getGuiLogger().err("Illegal Email");
						break;
					case ILLEGAL_PH:
						ArlithFrontend.getGuiLogger().err("Illegal Phone");
						break;
					case INVALID_EM:
						ArlithFrontend.getGuiLogger().err("Invalid Email");
						break;
					case INVALID_PH:
						ArlithFrontend.getGuiLogger().err("Invalid Phone");
						break;
					case LONG_EM:
						ArlithFrontend.getGuiLogger().err("Email too long");
						break;
					case LONG_PH:
						ArlithFrontend.getGuiLogger().err("Phone too long");
						break;
					case SHORT_EM:
						ArlithFrontend.getGuiLogger().err("Email too short");
						break;
					case SHORT_PH:
						ArlithFrontend.getGuiLogger().err("Phone too short");
						break;
					}
				});
				acting = false;
				return;
			} catch (Exception e4) {
				ArlithFrontend.getGuiLogger().err(e4);
				acting = false;
				return;
			} finally {
				centerPanel.setDisable(false);
			}

			Platform.runLater(() -> {
				try {
					(ArlithRuntime.window = new ArlithWindow(new ApplicationState(stage, client))).show(new HomePage())
							.display(stage);
				} catch (Exception e2) {
					client.stop();
					ArlithFrontend.getGuiLogger().err(e2);
					acting = false;
				}
			});
		});
		connectionThread.setDaemon(true);
		connectionThread.start();
	}

	// This lets you [Control + Enter] to log in even if the text box isn't focused.
	private final EventHandler<KeyEvent> globalEnterHandler = event -> {
		if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
			if (loginPanel.isVisible())
				onLogIn();
			else
				onCreateAccount();
			event.consume();
		}
	};

	@Override
	public void destroy() {
		bg.hide();
		stage.removeEventFilter(KeyEvent.KEY_PRESSED, globalEnterHandler);
		launchSettingsStage.hide();
	}

	@Override
	protected void show(Stage stage, ApplicationProperties properties) throws WindowLoadFailureException {
		this.stage = stage;
		stage.addEventFilter(KeyEvent.KEY_PRESSED, globalEnterHandler);
		stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.F3 && event.isControlDown() && event.isAltDown())
				launchSettingsStage.show();
		});
		stage.setMaximized(true);
		Scene scene;
		try {
			dbg("Opening Login GUI");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("LogInGUI.fxml"));
			loader.setController(this);
			root = loader.load();
			scene = stage.getScene() == null ? new Scene(root)
					: new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
			root.getStylesheets().addAll(properties.themeStylesheet.get(),
					"pala/apps/arlith/app/guis/login/login-gui.css");
			bg.getCanvas().widthProperty().bind(root.widthProperty());
			bg.getCanvas().heightProperty().bind(root.heightProperty());
			root.getChildren().add(0, bg.getCanvas());
			stage.setScene(scene);
			Thread motdReader = ArlithRuntime.newThread(Instance.CLIENT, () -> {
				try (Scanner s = new Scanner(ArlithRuntime.class.getResourceAsStream("motd.txt"))) {
					List<String> lines = new ArrayList<>();
					while (s.hasNextLine()) {
						lines.add(s.nextLine());
						if (Thread.interrupted())
							return;
					}
					Platform.runLater(
							() -> LogInWindow.this.motd.setText(lines.get((int) (Math.random() * lines.size()))));
				}
			});
			motdReader.setDaemon(true);
			motdReader.start();
			bg.show();
			stage.setMinWidth(400);
			stage.setMinHeight(600);
			dbg("Login GUI opened.");
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}
	}

}
