package pala.apps.arlith.launchers.testguiclient;

import javafx.application.Application;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;

public class TestGUIClientApplicationClass extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		new ClientGUIFrontend(primaryStage).launch();
	}

}
