package pala.apps.arlith.launchers.secondguitest;

import javafx.application.Application;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui2.login.LoginWindow;

public class SecondTestGUIClientApplicationClass extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> ArlithFrontend.getGuiLogger().err(e));
		ArlithFrontend.prepareStage(primaryStage);
		new LoginWindow().display(primaryStage);
	}

}
