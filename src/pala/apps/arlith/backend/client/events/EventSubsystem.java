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

import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.Logging;
import pala.apps.arlith.application.ArlithRuntime.Instance;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.backend.networking.encryption.MalformedResponseException;
import pala.apps.arlith.backend.networking.scp.CommunicationConnection;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventType;

public abstract class EventSubsystem {

	private final EventReader eventReifier;
	private EventManager<CommunicationProtocolEvent> eventManager;

	private Consumer<CommunicationProtocolError> errorHandler = Throwable::printStackTrace;

	public Consumer<CommunicationProtocolError> getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(Consumer<CommunicationProtocolError> errorHandler) {
		if (errorHandler == null)
			throw null;
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

	public EventSubsystem(CommunicationConnection client, EventReader eventReifier) {
		this.client = client;
		this.eventReifier = eventReifier;
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
					Logging.err(e3);
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
				Logging.err("Event connection handling a rate limit for reconnecting: " + e.getSleepTimeLong() + "ms.");
				try {
					Thread.sleep(e.getSleepTimeLong());
				} catch (InterruptedException e1) {
					return;
				}
				continue;
			} catch (CommunicationProtocolError e) {
				Logging.err("Encountered a CommunicationProtocolError when trying to authorize the event connection.");
			} catch (SocketTimeoutException e) {
				Logging.err("Timeout when connecting to server. Retrying...");
				continue;
			} catch (ClassCastException e) {
				Logging.err(
						"The server's response was malformed when reconnecting and authorizing the event connection. Retrying in 5s.");
			} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
					| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
					| IOException | MalformedResponseException e) {
				Logging.err("Failed to connect the event connection to the server. Retrying in 5s.");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Logging.err(e);
			}
		}
		Logging.std("Successfully reconnected the event connection.");
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
	 * @throws CommunicationProtocolError In case the server sends a {@link CommunicationProtocolError} of some sort.
	 */
	@SuppressWarnings("unchecked")
	protected void readEvent() throws CommunicationProtocolError {
		EventInstance<? extends CommunicationProtocolEvent> ei;
		try {
			ei = eventReifier.apply(client);
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw e;
		}
		eventManager.fire((EventType<CommunicationProtocolEvent>) ei.getType(), ei.getEvent());
	}

//	/**
//	 * This method is called upon the failure of a socket operation. It checks if
//	 * the {@link Client} has been "stopped" by checking if the event
//	 * thread is null. If it is, this {@link Client} is supposed to have
//	 * stopped.
//	 * 
//	 * @throws E The exception that caused the failure.
//	 */
//	private synchronized <E extends Exception> void checkFail(E e) throws E {
//		if (t != null)// Shutdown was NOT initiated; we encountered a stream error and so the stream
//						// is dead. We need to reconnect the stream manually.
//			reconnect();
//		throw e;
//	}
//
//	private synchronized void checkFail() {
//		if (t != null)
//			reconnect();
//	}

}
