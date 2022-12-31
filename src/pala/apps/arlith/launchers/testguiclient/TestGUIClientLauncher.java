package pala.apps.arlith.launchers.testguiclient;

import javafx.application.Application;
import pala.apps.arlith.launchers.ApplicationLauncher;

public class TestGUIClientLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		Application.launch(TestGUIClientApplicationClass.class, args);
	}

}
