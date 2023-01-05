package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class LogInPresentationImpl implements LogInPresentation {
	private final LogInLogic logic;
	private Scene scene;

	public LogInPresentationImpl(LogInLogic logic) {
		this.logic = logic;
	}

	public @FXML BorderPane root;
	public @FXML VBox logInBox, inputsBox;
	public @FXML HBox usernamePromptTextSection, passwordPromptTextSection;
	public @FXML Text title;
	public @FXML Button logInButton;
	private final SilverTextBox usernamePrompt = new SilverTextBox(), passwordPrompt = new SilverTextBox(true);

	private @FXML void initialize() {
		root.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(new RadialGradient(30, .2, .3, -.1, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, Color.DARKGRAY), new Stop(.4, Color.TRANSPARENT)), CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(new RadialGradient(0, 0, 1, .7, 1, true, CycleMethod.NO_CYCLE,
						new Stop(0, Color.DARKGRAY), new Stop(.4, Color.TRANSPARENT)), CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(
						new RadialGradient(-30, -.1, .2, .9, 1, true, CycleMethod.NO_CYCLE,
								new Stop(0, Color.color(.58, .58, .58)), new Stop(.4, Color.TRANSPARENT)),
						CornerRadii.EMPTY, Insets.EMPTY)));
		inputsBox.getChildren().add(0, usernamePrompt);
		inputsBox.getChildren().add(1, passwordPrompt);
		usernamePrompt.getPrompt().setText("Username:");
		passwordPrompt.getPrompt().setText("Password:");
		usernamePrompt.setPrefWidth(300);
		passwordPrompt.setPrefWidth(300);
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
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showLoginProblem(LoginProblemValue problem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockUIForLoggingIn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockUIForLoggingIn() {
		// TODO Auto-generated method stub

	}

}
