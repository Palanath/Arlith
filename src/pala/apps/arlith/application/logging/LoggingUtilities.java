package pala.apps.arlith.application.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.Flags;
import pala.apps.arlith.application.StandardLoggerImpl;
import pala.apps.arlith.libraries.Utilities;

/**
 * Class for general logging utilities in Arlith. Most loggers in the
 * application are created as an instance of {@link ConfiguredStandardLogger},
 * usually through the {@link #getConfiguredStandardLogger(String)} function.
 * Such loggers will automatically take into account the launch flags regarding
 * <i>File Logging</i> and <i>Debug Mode</i>, which are retrievable
 * {@link Flags#isFileLogging()} and {@link Flags#isDebugMode()}. That is to say
 * that instances of {@link ConfiguredStandardLogger} will obey the file-logging
 * flags set in {@link Arlith#getLaunchFlags() Arlith's Launch Flags} (including
 * the {@link Flags#getLogFileLocation() log file location} and
 * {@link Flags#isSeparateLogFiles() separate log files} flags) and the
 * debug-mode flag, so such instances will automatically log to the appropriate
 * files, and/or print debug messages if {@link Flags#isDebugMode()} is enabled.
 * 
 * @author Palanath
 *
 */
public class LoggingUtilities {
	/**
	 * <p>
	 * Streams that are set up by command line arguments during Arlith program
	 * launch.
	 * </p>
	 * <p>
	 * These {@link PrintStream}s are used by all logging utilities throughout the
	 * application that are created through
	 */
	private static PrintStream STANDARD_OUT, WARN_OUT, ERROR_OUT, DEBUG_OUT;

	public static PrintStream getStandardOut() {
		return STANDARD_OUT;
	}

	public static PrintStream getWarnOut() {
		return WARN_OUT;
	}

	public static PrintStream getErrorOut() {
		return ERROR_OUT;
	}

	public static PrintStream getDebugOut() {
		return DEBUG_OUT;
	}

	private static boolean fileLoggingRequestedAndFailed;

	/**
	 * Determines whether file logging was requested through
	 * {@link Flags#isFileLogging() command line flags} but that setting up file
	 * logging failed.
	 * 
	 * @return Whether file logging was requested and failed, or not.
	 */
	public static boolean isFileLoggingRequestedAndFailed() {
		return fileLoggingRequestedAndFailed;
	}

	/**
	 * <p>
	 * Creates and configures a {@link StandardLoggerImpl} that uses the standard,
	 * warning, error, and debug output streams configured through the command line.
	 * This simply returns an instance of {@link ConfiguredStandardLogger}
	 * instantiated with the given prefix.
	 * </p>
	 * 
	 * @param prefix The prefix for the logger.
	 * @return A new, configured {@link StandardLoggerImpl} instance.
	 */
	public static StandardLoggerImpl getConfiguredStandardLogger(String prefix) {
		return new ConfiguredStandardLogger(prefix);
	}

	/**
	 * An implementation of {@link StandardLoggerImpl} that is configured to send
	 * its logs to the {@link LoggingUtilities} class's
	 * {@link LoggingUtilities#getStandardOut()},
	 * {@link LoggingUtilities#getWarnOut()}, and other related print streams.
	 * 
	 * @author Palanath
	 *
	 */
	public static class ConfiguredStandardLogger extends StandardLoggerImpl {

		{
			if (STANDARD_OUT == null)
				if (Arlith.getLaunchFlags().isFileLogging()) {
					LocalDateTime time = Utilities.PROGRAM_LAUNCH_TIME;
					String timeString = time.getMonthValue() + '-' + time.getDayOfMonth() + '-' + time.getYear() + "--"
							+ time.getHour() + '-' + time.getMinute() + '-' + time.getSecond();
					try {
						File folderpath = new File(Arlith.getLaunchFlags().getLogFileLocation(), timeString);
						folderpath.mkdirs();// Attempt to make folder directory if it does not already exist.

						if (Arlith.getLaunchFlags().isSeparateLogFiles()) {
							STANDARD_OUT = new PrintStream(new File(folderpath, "standard-logs.log"));
							DEBUG_OUT = new PrintStream(new File(folderpath, "debug.log"));
							ERROR_OUT = new PrintStream(new File(folderpath, "errors.log"));
							WARN_OUT = new PrintStream(new File(folderpath, "warnings.log"));
						} else
							(STANDARD_OUT = DEBUG_OUT = ERROR_OUT = WARN_OUT = new PrintStream(
									new File(folderpath, "all.log")))
									.println("All log outputs are being forwarded to this log file.");
					} catch (FileNotFoundException e) {
						System.err.println(
								"An error occurred while setting file-based logging, so file-logs are not being used.");
						e.printStackTrace();
						fileLoggingRequestedAndFailed = true;
					}
				} else {
					STANDARD_OUT = DEBUG_OUT = WARN_OUT = System.out;
					ERROR_OUT = System.err;
				}
			setStdStream(STANDARD_OUT);
			setWrnStream(WARN_OUT);
			setDbgStream(DEBUG_OUT);
			setErrStream(ERROR_OUT);
			setLogDebugMessages(Arlith.getLaunchFlags().isDebugMode());
		}

		public ConfiguredStandardLogger(String prefix, DateTimeFormatter timestampFormatter, boolean logDebugMessages) {
			super(prefix, timestampFormatter, logDebugMessages);
		}

		public ConfiguredStandardLogger(String prefix, DateTimeFormatter timestampFormatter) {
			super(prefix, timestampFormatter);
		}

		public ConfiguredStandardLogger(String prefix) {
			super(prefix);
		}

	}
}
