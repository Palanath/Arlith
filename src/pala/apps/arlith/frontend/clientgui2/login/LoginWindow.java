package pala.apps.arlith.frontend.clientgui2.login;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import pala.libs.generic.guis.ApplicationProperties;
import pala.libs.generic.guis.Window;

public class LoginWindow extends Window {

	private @FXML BorderPane root;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	private @FXML void initialize() {
		BackgroundFill layer1 = new BackgroundFill(new RadialGradient(0, 0, 0, 0, 0.5, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#3235ff")), new Stop(1, Color.TRANSPARENT)), null, null);
		BackgroundFill layer2 = new BackgroundFill(Color.ORANGE, new CornerRadii(5), new Insets(5));
		root.setBackground(new Background(layer1, layer2));
	}

	@Override
	protected void show(Stage stage, ApplicationProperties properties) throws WindowLoadFailureException {
		stage.show();
	}

}
