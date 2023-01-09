package pala.apps.arlith;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import pala.apps.arlith.application.StandardLoggerImpl;
import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.application.logging.LoggingUtilities;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.launchers.ApplicationLauncher;
import pala.apps.arlith.launchers.jfxclient.JFXLauncher;
import pala.apps.arlith.libraries.Utilities;
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

	private static Flags LAUNCH_FLAGS;
	private static final String LAUNCHER_PACKAGE = ApplicationLauncher.class.getPackage().getName();
	/**
	 * <p>
	 * The {@link Logger} for the "main program." Loggers for the client and server
	 * (and its code) belong to the respective classes.
	 * </p>
	 * <p>
	 * The main program refers to the portion of execution that does not belong to
	 * either the client or the server. It usually entails code that neither the
	 * {@link ArlithClient} nor the {@link ArlithServer} execute, so all of the code
	 * in {@link #main(String[])} that runs before either the client or server are
	 * launched. Right now, typical execution of Arlith is execution of the main
	 * method, the client or server thread. In those instances, if any logging needs
	 * to be performed before either the client or server is launched, it can be
	 * done through this logger.
	 * </p>
	 * <p>
	 * This object should not be modified in most contexts.
	 * </p>
	 */
	private static StandardLoggerImpl LOGGER;

	public static Logger getLogger() {
		return LOGGER;
	}

	public static Flags getLaunchFlags() {
		return LAUNCH_FLAGS;
	}

	public static void main(String[] args) throws IOException {
		LAUNCH_FLAGS = new Flags(new CLIParams(args));

		LOGGER = LoggingUtilities.getConfiguredStandardLogger("ARLITH");

		// Check for various command line options.
		if (LAUNCH_FLAGS.isDebugMode())
			LOGGER.setLogDebugMessages(true);

		Utilities.setPreferredDestinationAddress(LAUNCH_FLAGS.getDefaultServerAddress());
		Utilities.setPreferredPort(LAUNCH_FLAGS.getDefaultServerPort());

		ApplicationLauncher launcher;
		// Launch the app.
		try {
			if (LAUNCH_FLAGS.isTestClient())
				launcher = (ApplicationLauncher) Class
						.forName(LAUNCHER_PACKAGE + ".testguiclient.TestGUIClientLauncher").getConstructor()
						.newInstance();
			else if (LAUNCH_FLAGS.isLaunchServer())
				launcher = (ApplicationLauncher) Class.forName(LAUNCHER_PACKAGE + ".terminalserver.ServerLauncher")
						.getConstructor().newInstance();
			else
				launcher = (ApplicationLauncher) Class.forName(LAUNCHER_PACKAGE + ".jfxclient.JFXLauncher")
						.getConstructor().newInstance();
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
		} catch (IllegalArgumentException e) {
			System.err.println(
					"Encountered a programmatic issue while launching Arlith. (This is VERY likely a developer bug, so please do report it if you can be bothered.)\n\tSpecifics: The launcher that was invoked as per the program arguments called an invalid constructor!!!");
			e.printStackTrace();
			return;
		} catch (InvocationTargetException e) {
			System.err.println("Trying to launch Arlith failed. The that occurred is printed below.");
			e.printStackTrace();
			return;
		} catch (NoSuchMethodException e) {
			System.err.println(
					"Encountered a programmatic issue during launch. (This is likely a developer bug so please report it if you can be bothered!)\n\tSpecifics: There's no constructor with no arguments that can be invoked to create an instance of the launcher being invoked.");
			e.printStackTrace();
			return;
		} catch (SecurityException e) {
			System.err.println(
					"An unexpected error occurred. SecurityExceptions were not considered during development. They really shouldn't show up. Please feel free to report this!");
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
