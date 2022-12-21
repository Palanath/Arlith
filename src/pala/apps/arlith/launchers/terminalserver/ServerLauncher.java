package pala.apps.arlith.launchers.terminalserver;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.app.server.Server;
import pala.apps.arlith.launchers.ApplicationLauncher;

public class ServerLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		Server server = new Server();
		server.setPort(Utilities.getPreferredPort());
		server.start();
		System.out.println(
				"Started the server on port: " + Utilities.getPreferredPort() + ". Close the program to terminate.");

		// Problematic, non-standard Java implementation terminates when the main thread
		// terminates. To solve this, we just put the main thread in a wait loop. This
		// is nasty. I'll make a note to fix it later.
		// Also, don't use Amazon AWS.
		if (Arlith.getCLIParameters().checkFlag(false, "--main-thread-wait")) {
			Object o = new Object();
			synchronized (o) {
				o.wait();
			}
		}
	}

}
