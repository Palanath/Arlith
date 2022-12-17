package pala.apps.arlith;

import java.io.IOException;

import javafx.application.Application;

public class JFXLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws IOException {
		Application.launch(JFXApplicationClass.class, args);
	}

}
