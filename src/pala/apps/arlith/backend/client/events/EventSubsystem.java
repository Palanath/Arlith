package pala.apps.arlith.backend.client.events;

import java.util.function.Consumer;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.ArlithRuntime.Instance;
import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ClientNetworkingBase;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventType;

public abstract class EventSubsystem extends ClientNetworkingBase {

	private final EventReader eventReifier;
	private final EventManager<CommunicationProtocolEvent> eventManager = new EventManager<>();
	/**
	 * <p>
	 * The {@link Logger} to which errors and other logging information will be
	 * sent. Various information is printed to the logger, such as warnings
	 * (whenever a {@link RateLimitError} is being handled), errors (whenever
	 * certain types of errors occur, and by default through the
	 * {@link #generalErrorHandler} and {@link #errorHandler}), and during normal
	 * operation (such as debug information). This must be non-<code>null</code>.
	 * </p>
	 * <p>
	 * Typically this starts out as the {@link Arlith#getLogger() main Arlith
	 * Application Logger} but gets set to the {@link ArlithClient#getLogger()
	 * client's logger} once this {@link EventSubsystem} becomes attached to a
	 * client. By default, this is {@link Arlith#getLogger()}.
	 * </p>
	 */
	private Logger logger = Arlith.getLogger();

	/**
	 * Sets the {@link Logger} that this {@link EventSubsystem} uses. This should
	 * not be <code>null</code>. See {@link #logger} for more details.
	 * 
	 * @param logger The non-<code>null</code> {@link Logger} to log messages to.
	 */
	public void setLogger(Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException("The logger cannot be null.");
		this.logger = logger;
	}

	/**
	 * Gets the {@link Logger} that this {@link EventSubsystem} uses.
	 * 
	 * @return The logger that this {@link EventSubsystem} logs messages to. See
	 *         {@link #logger} for more details.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * <p>
	 * Used to handle {@link CommunicationProtocolError}s that come up while the
	 * {@link EventSubsystem} is executing. This {@link Consumer} typically logs or
	 * prints the error somewhere.
	 * </p>
	 * <p>
	 * This method prints errors to the {@link #logger} by default using
	 * {@link Logger#err(Throwable)}.
	 * </p>
	 */
	private Consumer<? super CommunicationProtocolError> errorHandler;
	/**
	 * <p>
	 * Used to handle general errors that may come up while the
	 * {@link EventSubsystem} is executing. This {@link Consumer} typically logs or
	 * prints the error somewhere.
	 * </p>
	 * <p>
	 * This method prints errors to the {@link #logger} by default using
	 * {@link Logger#err(Throwable)}.
	 * </p>
	 */
	private Consumer<? super Exception> generalErrorHandler;

	public Consumer<? super Exception> getGeneralErrorHandler() {
		return generalErrorHandler;
	}

	public void setGeneralErrorHandler(Consumer<? super Exception> generalErrorHandler) {
		if (generalErrorHandler == null)
			throw new IllegalArgumentException("The general error handler cannot be null.");
		this.generalErrorHandler = generalErrorHandler;
	}

	public Consumer<? super CommunicationProtocolError> getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(Consumer<? super CommunicationProtocolError> errorHandler) {
		if (errorHandler == null)
			throw new IllegalArgumentException("The error handler should not be null.");
		this.errorHandler = errorHandler;
	}

	public EventManager<CommunicationProtocolEvent> getEventManager() {
		return eventManager;
	}

	private volatile Thread t;
	private boolean daemon = true;

	public EventSubsystem(EventReader eventReifier, Logger logger) {
		this.eventReifier = eventReifier;
		this.logger = logger;
		errorHandler = generalErrorHandler = logger::err;
	}

	// TODO Scan for callers of this constructor and change them so that they call
	// the other constructor and pass in the client's logger.
	public EventSubsystem(EventReader eventReifier) {
		this(eventReifier, Arlith.getLogger());
	}

	/**
	 * Starts this {@link EventSubsystem}, or does nothing if it is already started
	 * and in operation. Invoking this method causes the {@link EventSubsystem} to
	 * try to open a new {@link Connection} using {@link #prepareConnection()} and
	 * to, upon success, instantiate and start an internal <i>read</i>
	 * {@link Thread} which reads and dispatches incoming events from the
	 * connection.
	 */
	@Override
	public synchronized void start() {
		if (isRunning())
			return;
		super.start();
		t = ArlithRuntime.newThread(Instance.CLIENT, () -> {
			while (isRunning())
				try {
					readEvent();
				} catch (CommunicationProtocolError e2) {
					errorHandler.accept(e2);
				} catch (BlockException | UnknownCommStateException e) {
					try {
						restartConnection();
					} catch (InterruptedException e1) {
						return;
					}
				} catch (Exception e3) {
					generalErrorHandler.accept(e3);
				}
		});
		t.setDaemon(daemon);
		t.start();
	}

	@Override
	public synchronized void stop() {
		if (!isRunning())
			return;
		t.interrupt();
		super.stop();
	}

	/**
	 * Reads a single event from the input stream and fires handlers as appropriate.
	 * 
	 * @author Palanath
	 * @throws CommunicationProtocolError In case the server sends a
	 *                                    {@link CommunicationProtocolError} of some
	 *                                    sort.
	 * @throws BlockException             If a {@link BlockException} is encountered
	 *                                    while attempting to read from the
	 *                                    {@link Connection}.
	 * @throws UnknownCommStateException  If an {@link UnknownCommStateException} is
	 *                                    encountered while attempting to read from
	 *                                    the {@link Connection}.
	 */
	@SuppressWarnings("unchecked")
	protected void readEvent() throws CommunicationProtocolError, UnknownCommStateException, BlockException {
		EventInstance<? extends CommunicationProtocolEvent> ei;
		try {
			ei = eventReifier.apply(getConnection());
		} catch (RuntimeException e) {
			logger.err(e);
			throw e;
		}
		eventManager.fire((EventType<CommunicationProtocolEvent>) ei.getType(), ei.getEvent());
	}

}
