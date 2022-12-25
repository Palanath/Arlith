package pala.apps.arlith.backend.client.events;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.function.Consumer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.ArlithRuntime.Instance;
import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.libraries.networking.encryption.MalformedResponseException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventType;

public abstract class EventSubsystem {

	private final EventReader eventReifier;
	private EventManager<CommunicationProtocolEvent> eventManager;
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

	public void setEventManager(EventManager<CommunicationProtocolEvent> eventManager) {
		this.eventManager = eventManager;
	}

	public Thread getT() {
		return t;
	}

	public void setT(Thread t) {
		this.t = t;
	}

	private final CommunicationConnection client;
	private volatile Thread t;

	private boolean daemon = true;

	public EventSubsystem(CommunicationConnection client, EventReader eventReifier, Logger logger) {
		this.client = client;
		this.eventReifier = eventReifier;
		this.logger = logger;
		errorHandler = generalErrorHandler = logger::err;
	}

	// TODO Scan for callers of this constructor and change them so that they call
	// the other constructor and pass in the client's logger.
	public EventSubsystem(CommunicationConnection client, EventReader eventReifier) {
		this(client, eventReifier, Arlith.getLogger());
	}

	public void startup() {
		if (t != null)
			throw new IllegalStateException("Event Subsystem already running!");
		t = ArlithRuntime.newThread(Instance.CLIENT, () -> {
			while (t != null)
				try {
					readEvent();
				} catch (CommunicationProtocolError e2) {
					errorHandler.accept(e2);
				} catch (Exception e3) {
					generalErrorHandler.accept(e3);
				}
		});
		t.setDaemon(daemon);
		t.start();
	}

	protected abstract void authorize(CommunicationConnection connection) throws CommunicationProtocolError;

	public void reconnect() {
		stop();
		while (true) {
			try {
				// TODO Event
				client.start();
				authorize(client);
				break;

			} catch (RateLimitError e) {
				logger.wrn("Event connection handling a rate limit for reconnecting: " + e.getSleepTimeLong() + "ms.");
				try {
					Thread.sleep(e.getSleepTimeLong());
				} catch (InterruptedException e1) {
					return;
				}
				continue;
			} catch (CommunicationProtocolError e) {
				logger.err(
						"Encountered a CommunicationProtocolError when trying to authorize the event connection. The error will be printed below.");
				logger.err(e);
			} catch (SocketTimeoutException e) {
				logger.wrn("Timeout when connecting to server. Retrying...");
				continue;
			} catch (ClassCastException e) {
				logger.err(
						"The server's response was malformed when reconnecting and authorizing the event connection. Retrying in 5s, but usually this issue does not go away by just retrying. (Perhaps the client and server are running different versions.)");
			} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
					| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
					| IOException | MalformedResponseException e) {
				logger.err("Failed to connect the event connection to the server. Retrying in 5s.");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				return;
			}
		}
		logger.std("Successfully reconnected the event connection.");
		startup();
	}

	public synchronized void stop() {
		t = null;
		client.close();
	}

	/**
	 * Reads a single event from the input stream and fires handlers as appropriate.
	 * 
	 * @author Palanath
	 * @throws CommunicationProtocolError In case the server sends a
	 *                                    {@link CommunicationProtocolError} of some
	 *                                    sort.
	 */
	@SuppressWarnings("unchecked")
	protected void readEvent() throws CommunicationProtocolError {
		EventInstance<? extends CommunicationProtocolEvent> ei;
		try {
			ei = eventReifier.apply(client);
		} catch (RuntimeException e) {
			logger.err(e);
			throw e;
		}
		eventManager.fire((EventType<CommunicationProtocolEvent>) ei.getType(), ei.getEvent());
	}

}
