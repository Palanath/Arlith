package pala.apps.arlith.launchers.terminalserver;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.launchers.ApplicationLauncher;
import pala.apps.arlith.libraries.Utilities;

public class ServerLauncher implements ApplicationLauncher {

	@Override
	public void launchArlith(String... args) throws Exception {
		ArlithServer server = new ArlithServer();
		server.setPort(Utilities.getPreferredPort());
		server.start();
		server.setDaemon(false);
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
