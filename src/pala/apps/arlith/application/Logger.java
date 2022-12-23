package pala.apps.arlith.application;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.server.ArlithServer;

/**
 * <p>
 * Represents an object that manages command line information logging for an
 * API, framework, system, or component of Arlith. Both the {@link ArlithClient}
 * and {@link ArlithServer} maintain their own {@link Logger}s.
 * </p>
 * <p>
 * A {@link Logger} might print to only one output, e.g. {@link System#out}, or
 * may print to multiple, e.g. {@link System#out} for {@link #std(String)} and
 * {@link #dbg(String)} outputs, a <code>warning.log</code> file for
 * {@link #wrn(String)}, and an <code>error.log</code> file for
 * {@link #err(String)}. Such is up to the implementation.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Logger {
	/**
	 * Logs plain text. This text has no prefix or formatting.
	 * 
	 * @param text The text to log.
	 */
	void print(String text);

	/**
	 * Logs standard output text. This text typically has a prefix and, if
	 * available, shows up as green or a light blue. It's often used to denote
	 * success or normal progression.
	 * 
	 * @param text The text to log in the standard output format.
	 */
	void std(String text);

	/**
	 * Logs text intended to give a warning in regard to the app to who reads it.
	 * This typically has a prefix and is often used to log information noting that
	 * something is wrong or unexpected, but that normal operation is not impacted
	 * as a result. For example, {@link #wrn(String)} would be used to log text
	 * which expresses that an unnecessary operation failed, or an operation which
	 * slightly betters performance resulted in an exception, but the application
	 * can still be used normally. In colored contexts, this often shows up as
	 * orange or gold.
	 * 
	 * @param text The text to log in the warning output format.
	 */
	void wrn(String text);

	/**
	 * Logs text intended to inform for debugging purposes. This typically has a
	 * prefix and shows up in a blue, white, or gray color. Some implementations may
	 * not print this text (if they are to operate in production contexts, or
	 * non-debug contexts), or some may conditionally print this text, or have a
	 * flag to enable/disable printing debug text.
	 * 
	 * @param text The debug text to log.
	 */
	void dbg(String text);

	/**
	 * Logs text intended to notify or provide information about an important error.
	 * Typically, this is the most prominent type of logged text out of
	 * {@link #std(String)}, {@link #wrn(String)}, {@link #dbg(String)}, and itself,
	 * so implementations will far less often hide this type of text than the
	 * others. It is intended to provide information about the occurrence or cause
	 * of an error, or to shed light regarding an error. Such errors are
	 * functionality-breaking or inhibitory to the proper function of the
	 * application.
	 * 
	 * @param text The error text to log.
	 */
	void err(String text);
}
