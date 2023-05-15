package pala.apps.arlith.launchers.secondguitest;

import javafx.application.Application;
import pala.apps.arlith.launchers.ApplicationLauncher;

public class SecondTestGUIClientLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		Application.launch(SecondTestGUIClientApplicationClass.class, args);
	}

}
