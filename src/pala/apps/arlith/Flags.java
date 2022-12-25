package pala.apps.arlith;

import static pala.apps.arlith.libraries.Utilities.DEFAULT_DESTINATION_ADDRESS;
import static pala.apps.arlith.libraries.Utilities.DEFAULT_PORT;

import pala.libs.generic.parsers.cli.CLIParams;

public class Flags {
	private final boolean debugMode, launchServer, fileLogging, separateLogFiles;
	private final String defaultServerAddress;
	private final int defaultServerPort;

	public Flags(CLIParams params) {
		debugMode = params.checkFlag(false, "--debug", "-dbg");
		defaultServerAddress = params.readString(DEFAULT_DESTINATION_ADDRESS, "--server-address", "--servaddr",
				"--serv-addr", "-sa");
		defaultServerPort = params.readInt(DEFAULT_PORT, "--server-port", "--servprt", "--serv-prt", "-sp");
		launchServer = params.checkFlag(false, "--launch-server", "-ls");
		fileLogging = params.checkFlag(false, "--file-logging");
		separateLogFiles = params.checkFlag(false, "--separate-log-files");
	}

	/**
	 * Determines whether logging output is printed to a file instead of the
	 * standard out.
	 * 
	 * @return <code>true</code> if file logging is enabled, <code>false</code>
	 *         otherwise.
	 */
	public boolean isFileLogging() {
		return fileLogging;
	}

	/**
	 * Determines whether separate log files are used for each of the four types of
	 * logs (standard, warning, error, and debug). This flag only takes effect when
	 * {@link #isFileLogging() file logging} is enabled.
	 * 
	 * @return <code>true</code> if separate files will be used, <code>false</code>
	 *         otherwise.
	 */
	public boolean isSeparateLogFiles() {
		return separateLogFiles;
	}

	/**
	 * Determines whether debug mode is enabled. Debug mode causes debug messages to
	 * be logged.
	 * 
	 * @return Whether debug mode is enabled.
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * This flag causes the Arlith server to be launched instead of the Arlith
	 * client.
	 * 
	 * @return Whether the server or client is launched.
	 */
	public boolean isLaunchServer() {
		return launchServer;
	}

	/**
	 * This flag stores the default server address that the client attempts to
	 * connect to by default when launched. This does not affect the server if the
	 * server is launched (i.e. <code>-ls</code> or <code>--launch-server</code> is
	 * specified alongside this flag).
	 * 
	 * @return The default destination address that the client will attempt to
	 *         connect to.
	 */
	public String getDefaultServerAddress() {
		return defaultServerAddress;
	}

	/**
	 * Gets the port that the client will attempt to connect to the server on
	 * <i>and</i> the server will attempt to listen for connections on. The value of
	 * this flag will affect the application whether the client or server is
	 * launched.
	 * 
	 * @return The port to use for the client/server.
	 */
	public int getDefaultServerPort() {
		return defaultServerPort;
	}

}
