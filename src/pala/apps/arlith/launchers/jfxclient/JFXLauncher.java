package pala.apps.arlith.launchers.jfxclient;

import java.io.IOException;

import javafx.application.Application;
import pala.apps.arlith.launchers.ApplicationLauncher;

public class JFXLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws IOException {
		Application.launch(JFXApplicationClass.class, args);
	}

}
