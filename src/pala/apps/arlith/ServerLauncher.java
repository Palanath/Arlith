package pala.apps.arlith;

import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.app.server.Server;

public class ServerLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		Server server = new Server();
		server.setPort(Utilities.getPreferredPort());
		server.start();
		System.out.println(
				"Started the server on port: " + Utilities.getPreferredPort() + ". Close the program to terminate.");

	}

}
