package pala.apps.arlith;

import java.io.IOException;

import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.launchers.ApplicationLauncher;
import pala.apps.arlith.launchers.jfxclient.JFXLauncher;
import pala.libs.generic.parsers.cli.CLIParams;

/**
 * <p>
 * The main launch class for Arlith.
 * </p>
 * <p>
 * This class invokes the appropriate delegate launch class or the appropriate
 * code depending on what command line arguments are given. For example, if
 * either <code>-ls</code> or <code>--launch-server</code> is given as an
 * argument to the program, this class will launch a server instead of invoking
 * {@link JFXLauncher}.
 * </p>
 * 
 * @author Palanath
 *
 */
public class Arlith {

	private static CLIParams CLI_PARAMETERS;

	/**
	 * Gets the CLI Parameters for the application. This object is
	 * {@link CLIParams#isIgnoreCase() ignore-case} by default. This object should
	 * not be modified, but can be read or checked as needed.
	 * 
	 * @return The {@link CLIParams} that the application was launched with.
	 * 
	 * @author Palanath
	 */
	public static CLIParams getCLIParameters() {
		return CLI_PARAMETERS;
	}

	public static void main(String[] args) throws IOException {
		CLI_PARAMETERS = new CLIParams(args);
		CLI_PARAMETERS.setIgnoreCase(true);

		// Check for various command line options.
		if (CLI_PARAMETERS.checkFlag(false, "--debug", "-dbg"))
			Logging.setDebuggingEnabled(true);
		Utilities.setPreferredDestinationAddress(CLI_PARAMETERS.readString(Utilities.DEFAULT_DESTINATION_ADDRESS,
				"--server-address", "--servaddr", "--serv-addr", "-sa"));
		Utilities.setPreferredPort(
				CLI_PARAMETERS.readInt(Utilities.DEFAULT_PORT, "--server-port", "--servprt", "--serv-prt", "-sp"));

		ApplicationLauncher launcher;
		// Launch the app.
		try {
			if (CLI_PARAMETERS.checkFlag(false, "--launch-server", "-ls"))
				launcher = (ApplicationLauncher) Class.forName("pala.apps.arlith.ServerLauncher").newInstance();
			else
				launcher = (ApplicationLauncher) Class.forName("pala.apps.arlith.JFXLauncher").newInstance();
		} catch (InstantiationException e) {
			System.err.println("An error occurred while trying to launch Arlith.");
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			System.err.println(
					"Encountered programmatic issue while launching Arlith. (This is likely a developer bug! Please report it if you can be bothered.)\n\tSpecifics: The launcher that was invoked as per the program arguments, if any, does not have an accessible constructor, (perhaps the constructor is private, or the protected/package-private and the launcher is in a different package?), so trying to construct an instance of the launcher failed.");
			e.printStackTrace();
			return;
		} catch (ClassNotFoundException e) {
			System.err.println(
					"Failed to launch Arlith. An attempt was made to load a class that was not found. It could be the case that a certain library is missing from your Java installation (e.g. JavaFX) or that you downloaded an instance of Arlith that does not come with certain features (e.g. you can't run an Arlith server without a build containing server code). It *could* also be a developer error, in which case the Launcher class that is supposed to be invoked to Launch Arlith is not accessible. This normally happens when the class is moved somewhere and the code referring to it still points to the old location.");
			e.printStackTrace();
			return;
		}

		try {
			launcher.launchArlith(args);
		} catch (Exception e) {
			System.err.println(
					"An error occurred while launching Arlith. The error will be printed right after this. If you report this, please also report what launch flags you ran Arlith with (if any).");
			e.printStackTrace();
		}
	}

}
