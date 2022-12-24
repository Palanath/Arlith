package pala.apps.arlith.application;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class StandardLoggerImpl implements Logger {

	private final String prefix;
	private final boolean includeTimestamps;

	public StandardLoggerImpl(String prefix, boolean includeTimestamps) {
		this.prefix = prefix;
		this.includeTimestamps = includeTimestamps;
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
	private String createFullPrefix() {
		return (includeTimestamps ? '<' + DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now()) + ">[" : '[')
				+ prefix + "]: ";
	}

}
