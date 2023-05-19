package pala.apps.arlith.frontend.clientgui2.login;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
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
		// Border
//		BackgroundFill layer1 = new BackgroundFill(new RadialGradient(0, 0, 0, 0, 1.5, true, CycleMethod.NO_CYCLE,
//				new Stop(.6, Color.web("#3235ff")), new Stop(.7, Color.web("#ff8b32"))), null, null);
//		BackgroundFill layer2 = new BackgroundFill(new RadialGradient(0, 0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
//				new Stop(.8, Color.web("#c232ff")), new Stop(1, Color.TRANSPARENT)), null, null);

		// Solid Fill
		BackgroundFill layer3 = new BackgroundFill(Color.gray(.12), null, null);

		// Stripes
		Color stripeColor = Color.gray(.2);
		double stripeWidth = .04;
		BackgroundFill layer4 = new BackgroundFill(new LinearGradient(0, 0, 20, 20, false, CycleMethod.REPEAT,
				new Stop(0, stripeColor), new Stop(stripeWidth, stripeColor), new Stop(stripeWidth, Color.TRANSPARENT),
				new Stop(1, Color.TRANSPARENT)), null, null);
		BackgroundFill layer5 = new BackgroundFill(new LinearGradient(0, 20, 20, 0, false, CycleMethod.REPEAT,
				new Stop(0, stripeColor), new Stop(stripeWidth, stripeColor), new Stop(stripeWidth, Color.TRANSPARENT),
				new Stop(1, Color.TRANSPARENT)), null, null);

		// Lights
		BackgroundFill layer6 = new BackgroundFill(new RadialGradient(0, 0, -.25, 0, 1.05, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#3235ff3f")), new Stop(1, Color.TRANSPARENT)), null,
				null);
		BackgroundFill layer7 = new BackgroundFill(new RadialGradient(0, 0, 1.2, .8, .8, true, CycleMethod.NO_CYCLE,
				new Stop(0, Color.web("#ff8b323f")), new Stop(1, Color.TRANSPARENT)), null,
				null);

		root.setBackground(new Background(layer3, layer4, layer5, layer6, layer7));
	}

	@Override
	protected void show(Stage stage, ApplicationProperties properties) throws WindowLoadFailureException {
		stage.show();
		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		try {
			stage.setScene(new Scene(loader.load(getClass().getResourceAsStream("LoginGUI.fxml"))));
		} catch (IOException e) {
			throw new WindowLoadFailureException("IOException while loading embedded UI for login window.", e);
		}
	}

}
