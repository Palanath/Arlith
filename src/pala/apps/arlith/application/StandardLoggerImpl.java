package pala.apps.arlith.application;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * An implementation of {@link Logger} that prints to the standard output and
 * standard error. This class does not depend on any frameworks or APIs other
 * than {@link Instant}, {@link DateTimeFormatter}, and {@link System#out} and
 * {@link System#err}.
 * 
 * @author Palanath
 *
 */
public class StandardLoggerImpl implements Logger {

	/**
	 * The prefix used by the logger. <span style="color: red;">This should
	 * <b>not</b> be <code>null</code>.</span> This is typically the name system or
	 * component of the application using this logger. For example, a value of
	 * <code>CLIENT</code> will cause <code>[CLIENT]: </code> to be prepended in
	 * front of formatted log messages.
	 */
	private String prefix;
	/**
	 * The formatter used to format timestamps for the logger. This <b>can</b> be
	 * left <code>null</code>, in which case timestamps will not be printed. By
	 * default, it is the {@link DateTimeFormatter#RFC_1123_DATE_TIME}.
	 */
	private DateTimeFormatter timestampFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
	/**
	 * Whether or not debug messages are logged. This is <code>false</code> by
	 * default.
	 */
	private boolean logDebugMessages;

	/**
	 * Sets the timestamp formatter used to format the timestamps printed into log
	 * messages. By default, this is the
	 * {@link DateTimeFormatter#RFC_1123_DATE_TIME}. Set to <code>null</code> to
	 * disable including timestamps in log messages. The formatter should be able to
	 * handle {@link Instant} objects.
	 * 
	 * @param timestampFormatter The formatter used to format timestamps in log
	 *                           messages, or <code>null</code> to disable
	 *                           timestamping log messages.
	 */
	public void setTimestampFormatter(DateTimeFormatter timestampFormatter) {
		this.timestampFormatter = timestampFormatter;
	}

	/**
	 * Returns the timestamp formatter used to format the timestamps printed into
	 * log messages, or <code>null</code> if timestamp formatting is disabled.
	 * 
	 * @return
	 */
	public DateTimeFormatter getTimestampFormatter() {
		return timestampFormatter;
	}

	/**
	 * Enables or disables whether debug messages are logged. This is
	 * <code>false</code> by default.
	 * 
	 * @param logDebugMessages
	 */
	public void setLogDebugMessages(boolean logDebugMessages) {
		this.logDebugMessages = logDebugMessages;
	}

	/**
	 * Returns whether debug messages are logged by this {@link StandardLoggerImpl}.
	 * This setting can be changed through {@link #setLogDebugMessages(boolean)}.
	 * 
	 * @return Whether debug messages are logged.
	 */
	public boolean isLogDebugMessages() {
		return logDebugMessages;
	}

	/**
	 * Returns the prefix associated with this {@link StandardLoggerImpl}. This is a
	 * non-<code>null</code> {@link String} that typically represents the name of
	 * the component or system using this {@link Logger}.
	 * 
	 * @return The prefix being used.
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Sets or changes the prefix associated with this {@link StandardLoggerImpl}.
	 * This is a non-<code>null</code> {@link String} that typically represents the
	 * name of the component or system using this {@link Logger}.
	 * 
	 * @param prefix The prefix to use. This should not be <code>null</code>.
	 */
	public void setPrefix(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("Prefix should not be null.");
		this.prefix = prefix;
	}

	/**
	 * Creates a {@link StandardLoggerImpl} with
	 * {@link DateTimeFormatter#RFC_1123_DATE_TIME} time formatting, debug logging
	 * disabled, and the specified prefix.
	 * 
	 * @param prefix The prefix used in front of messages. For more information, see
	 *               {@link #prefix} or {@link #getPrefix()}. This parameter should
	 *               never be <code>null</code>.
	 */
	public StandardLoggerImpl(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("Prefix should not be null.");
		this.prefix = prefix;
	}

	/**
	 * Creates a {@link StandardLoggerImpl} with the specified prefix, timestamp
	 * formatter, and choice of whether to log debug messages.
	 * 
	 * @param prefix             The prefix used in front of messages. For more
	 *                           information, see {@link #prefix} or
	 *                           {@link #getPrefix()}. This parameter should never
	 *                           be <code>null</code>.
	 * @param timestampFormatter The formatter used to format timestamps, or
	 *                           <code>null</code> if timestamps should not be
	 *                           printed in each log message.
	 * @param logDebugMessages   Whether debug messages should be logged.
	 */
	public StandardLoggerImpl(String prefix, DateTimeFormatter timestampFormatter, boolean logDebugMessages) {
		this(prefix, timestampFormatter);
		this.logDebugMessages = logDebugMessages;
	}

	/**
	 * Creates a {@link StandardLoggerImpl} with the specified prefix and timestamp
	 * formatter.
	 * 
	 * @param prefix             The prefix used in front of messages. For more
	 *                           information, see {@link #prefix} or
	 *                           {@link #getPrefix()}. This parameter should never
	 *                           be <code>null</code>.
	 * @param timestampFormatter The formatter used to format timestamps, or
	 *                           <code>null</code> if timestamps should not be
	 *                           printed in each log message.
	 */
	public StandardLoggerImpl(String prefix, DateTimeFormatter timestampFormatter) {
		this(prefix);
		this.timestampFormatter = timestampFormatter;
	}

	@Override
	public void print(String text) {
		System.out.println(text);
	}

	/**
	 * Logs information to the standard output.
	 */
	@Override
	public void std(String text) {
		System.out.println(createFullPrefix() + text);
	}

	/**
	 * Logs warning information. Logged text is printed to the standard output with
	 * a <code>(WARN): </code> prefix.
	 */
	@Override
	public void wrn(String text) {
		System.out.println(createFullPrefix() + "(WARN): " + text);
	}

	/**
	 * Logs debug information. Logged text is printed to the standard output with a
	 * <code>(DEBUG): </code> prefix.
	 */
	@Override
	public void dbg(String text) {
		if (logDebugMessages)
			System.out.println(createFullPrefix() + "(DEBUG): " + text);
	}

	/**
	 * Logs errors and related information. Logged text is printed to the standard
	 * error output.
	 */
	@Override
	public void err(String text) {
		System.err.println(createFullPrefix() + text);
	}

	/**
	 * <p>
	 * Creates the general prefix used for all of {@link #std(String)},
	 * {@link #wrn(String)}, {@link #dbg(String)}, and {@link #err(String)}. This
	 * method is called by the logging method immediately before the logging takes
	 * place.
	 * </p>
	 * <p>
	 * By default, it returns a string composed of the timestamp followed by the
	 * {@link #prefix}, both in a special format. The timestamp is obtained using
	 * {@link Instant#now()} and is formatted using the
	 * {@link DateTimeFormatter#RFC_1123_DATE_TIME} formatter. Textually, it's
	 * wrapped in chevrons (<code>&lt;</code> and <code>&gt;</code>). The prefix
	 * follows is immediately and is wrapped in brackets (<code>[</code> and
	 * <code>]</code>). The string is followed by a colon and a space character.
	 * </p>
	 * 
	 * @return The prefix to be used for formatted log outputs.
	 */
	protected String createFullPrefix() {
		return (timestampFormatter == null ? '<' + timestampFormatter.format(Instant.now()) + ">[" : '[') + prefix
				+ "]: ";
	}

}
