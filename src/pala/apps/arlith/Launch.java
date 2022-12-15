package pala.apps.arlith;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;
import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.app.guis.GUIUtils;
import pala.apps.arlith.app.guis.login.LogInWindow;
import pala.apps.arlith.app.logging.Logging;
import pala.libs.generic.guis.Window;
import pala.libs.generic.parsers.cli.CLIParams;

public class Launch extends Application {

	@Override
	public void start(Stage arg0) throws Exception {
		Window.getDefaultApplicationProperties().themeStylesheet
				.put("/pala/apps/arlith/graphics/stylesheets/default-styles.css");
		GUIUtils.prepareStage(arg0);
		arg0.show();
		new LogInWindow().display(arg0);
	}

	public static void main(String[] args) throws IOException {
		CLIParams params = new CLIParams(args);
		params.setIgnoreCase(true);
		if (params.checkFlag(false, "--debug", "-dbg"))
			Logging.setDebuggingEnabled(true);
		Utilities.setPreferredDestinationAddress(params.readString(Utilities.DEFAULT_DESTINATION_ADDRESS,
				"--server-address", "--servaddr", "--serv-addr", "-sa"));
		Utilities.setPreferredPort(
				params.readInt(Utilities.DEFAULT_PORT, "--server-port", "--servprt", "--serv-prt", "-sp"));

		Application.launch(args);
	}

}
