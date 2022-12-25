package pala.apps.arlith.backend.server;

import java.io.File;
import java.net.Socket;
import java.util.Map;

import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.StandardLoggerImpl;
import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.application.logging.LoggingUtilities;
import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.server.contracts.serversystems.EventConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestMapper;
import pala.apps.arlith.backend.server.contracts.world.ServerWorld;
import pala.apps.arlith.backend.server.systems.AuthenticationSystem;
import pala.apps.arlith.backend.server.systems.EventSystem;
import pala.apps.arlith.backend.server.systems.RequestSystemImpl;
import pala.apps.arlith.backend.server.world.ServerWorldImpl;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Communicator;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnectionAcceptor;

public class ArlithServer extends CommunicationConnectionAcceptor {

	/**
	 * Used for logging regarding general server operations and messages. For
	 * thread-specific logging, see
	 */
	private final Logger logger = LoggingUtilities.getConfiguredStandardLogger("SERVER");
	private final ServerWorld world = new ServerWorldImpl(new File("arlith-data"), this);
	private final RequestSystemImpl requestManager = new RequestSystemImpl(this);
	/**
	 * Manages all forms of user authentication in the application. This can be
	 * queried to check if a user provided the right {@link AuthToken} or log-in
	 * information when authenticating a connection.
	 */
	private final AuthenticationSystem authSystem = new AuthenticationSystem();
	/**
	 * Tracks all of the {@link EventConnection}s that are logged in. Every
	 * {@link EventConnection} that is logged in under a certain user is tracked.
	 * This object also allows convenient means of firing events to
	 * {@link EventConnection}s and multiple users at the same time.
	 */
	private final EventSystem eventSystem = new EventSystem();

	private static final Object THREAD_LOGGER_KEY = new Object();

	/**
	 * <p>
	 * Gets the {@link Logger} associated with the specified server thread. This
	 * often has a special prefix denoting some unique information about the thread,
	 * e.g. what user is connected on it.
	 * </p>
	 * <p>
	 * If the specified {@link Thread} does not have an associated {@link Logger},
	 * this method throws an {@link IllegalArgumentException}, as it is assumed that
	 * the server did not create the thread.
	 * </p>
	 * 
	 * @param thread The {@link Thread} to get the {@link Logger} for.
	 * @return The {@link Logger}.
	 */
	public static Logger getThreadLogger(Thread thread) {
		Logger logger = (Logger) ArlithRuntime.getThreadData(thread).get(THREAD_LOGGER_KEY);
		if (logger == null)
			throw new IllegalArgumentException(
					"The specified thread has no associated logger; the thread seems to not belong to the server.");
		return logger;
	}

	/**
	 * Gets the {@link Logger} associated with the current server thread. See
	 * {@link #getThreadLogger(Thread)} for more details.
	 * 
	 * @return The result of calling {@link #getThreadLogger(Thread)} with the
	 *         result of a call to {@link Thread#currentThread()} as the only
	 *         argument.
	 */
	public static Logger getThreadLogger() {
		return getThreadLogger(Thread.currentThread());
	}

	/**
	 * Gets the general purpose {@link Logger} for the server. This is used to log
	 * messages are sourced from the main server thread or are not specific to a
	 * server child thread.
	 * 
	 * @return The server's {@link Logger}.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * <p>
	 * Sets up a new thread's {@link Logger}. Threads that have undergone a call to
	 * this method can have their {@link Logger}s retrieved from anywhere using
	 * {@link #getThreadLogger()} or {@link #getThreadLogger(Thread)}.
	 * </p>
	 * <p>
	 * The <code>purpose</code> provided is used in the prefix for the logger, and
	 * can be changed later using
	 * {@link #changeThreadLoggerPurpose(Thread, String)}. The prefix provided to
	 * the {@link Logger} is the string <code>"SERVER::"</code> followed by the
	 * specified <code>purpose</code>. Typically, the purpose is something such as
	 * the IP of the connecting client, and then, once more information becomes
	 * available (i.e., the client logs in), the purpose is
	 * {@link #changeThreadLoggerPurpose(Thread, String) changed} to the client's
	 * tag.
	 * </p>
	 * <p>
	 * This function should not be called more than once with the same
	 * {@link Thread}. If such is done, an {@link IllegalArgumentException} will be
	 * raised.
	 * </p>
	 * 
	 * @param thread  The {@link Thread} to setup the {@link Logger} for.
	 * @param purpose The purpose of the thread; a short string, often uniquely
	 *                identifying, and elaborating on the purpose of, the thread.
	 * @return The {@link Logger} that the thread was given. This is the same
	 *         {@link Logger} that would be obtained from a call to
	 *         {@link #getThreadLogger(Thread)} made with the same {@link Thread}
	 *         argument, after this function is run.
	 */
	private static Logger setupThreadLogger(Thread thread, String purpose) {
		Map<Object, Object> threadData = ArlithRuntime.getThreadData(thread);
		if (threadData.containsKey(THREAD_LOGGER_KEY))
			throw new IllegalArgumentException("The specified thread (" + thread.getName() + ", with given purpose \""
					+ purpose + "\"), already has a logger associated with it.");
		StandardLoggerImpl logger = LoggingUtilities.getConfiguredStandardLogger("SERVER::" + purpose);
		threadData.put(THREAD_LOGGER_KEY, logger);
		return logger;
	}

	/**
	 * Sets up the current {@link Thread}'s thread logger. See
	 * {@link #setupThreadLogger(Thread, String)} for more details.
	 * 
	 * @param purpose The purpose of the thread. See
	 *                {@link #setupThreadLogger(Thread, String)} for more details.
	 * @return A call to {@link #setupThreadLogger(Thread, String)} invoked with the
	 *         result of calling {@link Thread#currentThread()} as the first
	 *         argument.
	 */
	private static Logger setupThreadLogger(String purpose) {
		return setupThreadLogger(Thread.currentThread(), purpose);
	}

	/**
	 * <p>
	 * Changes the <i>purpose</i> of the specified {@link Thread} for logging
	 * purposes. The purpose of the thread is used in the prefix of its
	 * {@link Logger}. See {@link #setupThreadLogger(Thread, String)} for more
	 * details.
	 * </p>
	 * <p>
	 * Note that this function will raise an {@link IllegalArgumentException} if the
	 * specified thread has not been set up already using
	 * {@link #setupThreadLogger(Thread, String)} or
	 * {@link #setupThreadLogger(String)}.
	 * </p>
	 * 
	 * @param thread  The {@link Thread} to change the {@link Logger} purpose of.
	 * @param purpose The new <code>purpose</code>.
	 */
	public static void changeThreadLoggerPurpose(Thread thread, String purpose) {
		Map<Object, Object> threadData = ArlithRuntime.getThreadData(thread);
		if (!threadData.containsKey(THREAD_LOGGER_KEY))
			throw new IllegalArgumentException("The specified thread (" + thread.getName() + ", with given purpose \""
					+ purpose
					+ "\"), does not already have a logger associated with it, so its purpose cannot be changed.");
		StandardLoggerImpl logger = (StandardLoggerImpl) threadData.get(THREAD_LOGGER_KEY);
		logger.setPrefix("SERVER::" + purpose);
	}

	/**
	 * <p>
	 * Changes the <i>purpose</i> of the current {@link Thread} for logging
	 * purposes. See {@link #changeThreadLoggerPurpose(Thread, String)} for more
	 * details.
	 * </p>
	 * <p>
	 * This function invokes {@link Thread#currentThread()} and calls
	 * {@link #changeThreadLoggerPurpose(Thread, String)} with the result as the
	 * first argument.
	 * </p>
	 * 
	 * @param newPurpose The new purpose of the current thread.
	 */
	public static void changeThreadLoggerPurpose(String newPurpose) {
		changeThreadLoggerPurpose(Thread.currentThread(), newPurpose);
	}

	@Override
	protected void acceptSocket(Socket incomingSocketConnection) throws Exception {
		// This is called on a new thread (unless
		// CommunicationConnectionAcceptor#acceptOnNewThread is set to false manually).
		Logger logger = setupThreadLogger(
				incomingSocketConnection.getInetAddress().getHostAddress() + ':' + incomingSocketConnection.getPort());
		logger.std("Connection Established.");

		Communicator communicator = new Communicator(incomingSocketConnection);

		final RequestConnection connection = requestManager.new RequestConnectionImpl(communicator);
		while (connection.active())
			try {
				requestManager.handleRequest(connection);
			} catch (ClassCastException | MalformedIncomingRequestException | RequestNotSupportedException e) {
				logger.err(e);

			} catch (UnknownCommStateException | BlockException e) {
				// Connection issue or connection reset.
				logger.std("Disconnected.");
				communicator.close();
				break;
			}

		// Unregister it if it was registered by an authorization request handler.
		requestManager.unregisterRequestClient(connection);
	}

	@Override
	protected void acceptConnection(final Connection communicator) {
		// Let #acceptSocket(Socket) handle.
	}

	public AuthenticationSystem getAuthSystem() {
		return authSystem;
	}

	public EventSystem getEventSystem() {
		return eventSystem;
	}

	public RequestMapper getRequestManager() {
		return requestManager;
	}

	public ServerWorld getWorld() {
		return world;
	}

}
