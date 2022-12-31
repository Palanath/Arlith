package pala.apps.arlith;

import static pala.apps.arlith.libraries.Utilities.DEFAULT_DESTINATION_ADDRESS;
import static pala.apps.arlith.libraries.Utilities.DEFAULT_PORT;

import pala.apps.arlith.launchers.testguiclient.TestGUIClientLauncher;
import pala.libs.generic.parsers.cli.CLIParams;

public class Flags {
	private final boolean debugMode, launchServer, fileLogging, separateLogFiles, testClient;
	private final String defaultServerAddress, logFileLocation;
	private final int defaultServerPort;

	public Flags(CLIParams params) {
		debugMode = params.checkFlag(false, "--debug", "-dbg");
		defaultServerAddress = params.readString(DEFAULT_DESTINATION_ADDRESS, "--server-address", "--servaddr",
				"--serv-addr", "-sa");
		defaultServerPort = params.readInt(DEFAULT_PORT, "--server-port", "--servprt", "--serv-prt", "-sp");
		launchServer = params.checkFlag(false, "--launch-server", "-ls");
		fileLogging = params.checkFlag(false, "--file-logging");
		separateLogFiles = params.checkFlag(false, "--separate-log-files");
		logFileLocation = params.readString("arlith-logs", "--log-file-location");
		testClient = params.checkFlag(false, "--test-client");
	}

	/**
	 * <p>
	 * Used for testing. If this flag is set, the {@link TestGUIClientLauncher} is
	 * invoked.
	 * </p>
	 * 
	 * @return Whether the Client Test GUI launcher will be invoked upon startup or
	 *         not.
	 */
	public boolean isTestClient() {
		return testClient;
	}

	/**
	 * <p>
	 * Specifies what folder log files are stored in. The log file is put in a
	 * sub-directory of this directory and are named based on whether log files are
	 * split or different types of messages are being written to the same file (see
	 * {@link #isSeparateLogFiles()}).
	 * </p>
	 * <p>
	 * The default value of this flag is <code>"arlith-logs"</code>. Inside this
	 * directory, a new directory is made whose name is string representing the time
	 * that the program started up, in the format <code>MM-dd-yyyy--hh-mm-ss</code>,
	 * where <code>MM</code> is up to two digits representing the month,
	 * <code>dd</code> is up to two digits representing the day of the month,
	 * <code>yyyy</code> is four digits representing the year, <code>hh</code> is up
	 * to two digits representing the hour of the day, <code>mm</code> is up to two
	 * digits representing the minute of the hour, and <code>ss</code> is up to two
	 * digits representing the second of the minute. Log files for the program are
	 * written inside this time-directory.
	 * </p>
	 * 
	 * @flag --log-file-location
	 * @return The location that log files are placed in.
	 */
	public String getLogFileLocation() {
		return logFileLocation;
	}

	/**
	 * Determines whether logging output is printed to a file instead of the
	 * standard out.
	 * 
	 * @flag --file-logging
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
	 * @flag --separate-log-files
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
	 * @flag --debug -dbg
	 * @return Whether debug mode is enabled.
	 */
	public boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * This flag causes the Arlith server to be launched instead of the Arlith
	 * client.
	 * 
	 * @flag --launch-server -ls
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
	 * @flag --server-address --servaddr --serv-addr -sa
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
	 * @flag --server-port --servprt --serv-prt -sp
	 * @return The port to use for the client/server.
	 */
	public int getDefaultServerPort() {
		return defaultServerPort;
	}

}
