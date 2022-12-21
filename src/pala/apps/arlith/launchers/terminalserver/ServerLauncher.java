package pala.apps.arlith.launchers.terminalserver;

import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.launchers.ApplicationLauncher;
import pala.apps.arlith.libraries.Utilities;

public class ServerLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		ArlithServer server = new ArlithServer();
		server.setPort(Utilities.getPreferredPort());
		server.setDaemon(false);
		server.start();
		System.out.println(
				"Started the server on port: " + Utilities.getPreferredPort() + ". Close the program to terminate.");
	}

}
