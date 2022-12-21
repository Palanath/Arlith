package pala.apps.arlith.libraries.networking.scp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.application.ArlithRuntime.Instance;
import pala.apps.arlith.libraries.networking.Communicator;
import pala.apps.arlith.libraries.networking.Connection;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventSystem;

public abstract class CommunicationConnectionAcceptor implements EventSystem<ServerEvent> {

	private final EventManager<ServerEvent> eventManager = new EventManager<>();

	@Override
	public EventManager<ServerEvent> getEventManager() {
		return eventManager;
	}

	private int port = 0, backlog = 50;

	public void setPort(int port) {
		this.port = port;
	}

	private volatile IOException excep;

	/**
	 * This method attempts to close the {@link ServerSocket} associated with this
	 * {@link CommunicationConnectionAcceptor}. If anything fails, this method fires
	 * a ServerSocketCloseFailure event and simply returns silently.
	 */
	private void closeServSock() {
		try {
			sock.close();
		} catch (IOException e) {
			eventManager.fire(ServerSocketCloseFailureEvent.SERVER_SOCKET_CLOSE_FAILURE_EVENT,
					new ServerSocketCloseFailureEvent(this, e, sock));
		}
		sock = null;
	}

	private volatile boolean stopped;
	private boolean acceptOnNewThread = true;

	public boolean isAcceptOnNewThread() {
		return acceptOnNewThread;
	}

	public void setAcceptOnNewThread(boolean acceptOnNewThread) {
		this.acceptOnNewThread = acceptOnNewThread;
	}

	private Thread getNewThread() {
		Thread thread = new Thread() {

			{
				setDaemon(true);
			}

			@Override
			public void run() {
				// The start method already has a lock when run() is called, so we need to wait
				// until it's paused to continue.
				synchronized (CommunicationConnectionAcceptor.this) {
					try {
						sock = new ServerSocket();
						sock.setReuseAddress(true);// Allow the next servSock to bind right after this one closes if
													// necessary.
						sock.bind(new InetSocketAddress(port), backlog);
					} catch (IOException e) {
						excep = e;
						CommunicationConnectionAcceptor.this.notify();
						return;
					}
					CommunicationConnectionAcceptor.this.notify();
				}

				while (!stopped) {
					Socket sck;
					try {
						sck = sock.accept();
					} catch (IOException e) {
						// An error occurred while trying to accept a connection. One of these errors
						// could be that the socket was closed. This should be indicated by the value of
						// the #stopped variable.

						closeServSock();

						if (!stopped) {
							// An error occurred and this was not a manual stop.
							eventManager.fire(ServerStoppedAbruptlyEvent.SERVER_STOPPED_ABRUPTLY_EVENT,
									new ServerStoppedAbruptlyEvent(CommunicationConnectionAcceptor.this, e));
							// Attempt to restart the thread system.
							restartThreadSystem();
						}
						return;
					}

					if (acceptOnNewThread) {
						Thread thread = new Thread(() -> {
							try {
								acceptSocket(sck);
							} catch (Exception e) {
								eventManager.fire(ServerSocketAcceptFailureEvent.SERVER_SOCKET_ACCEPT_FAILURE_EVENT,
										new ServerSocketAcceptFailureEvent(CommunicationConnectionAcceptor.this, excep,
												sck));
							}
						});
						ArlithRuntime.register(Instance.SERVER, thread);
						thread.setDaemon(true);
						thread.start();
					} else {
						try {
							acceptSocket(sck);
						} catch (Exception e) {
							eventManager.fire(ServerSocketAcceptFailureEvent.SERVER_SOCKET_ACCEPT_FAILURE_EVENT,
									new ServerSocketAcceptFailureEvent(CommunicationConnectionAcceptor.this, excep,
											sck));
						}
					}
				}

				closeServSock();
			}

			private void restartThreadSystem() {
				eventManager.fire(ServerThreadSystemRestartingEvent.SERVER_THREAD_SYSTEM_RESTARTING_EVENT,
						new ServerThreadSystemRestartingEvent(CommunicationConnectionAcceptor.this));
				runner = getNewThread();

				// Lock on this object while starting the thread and opening the socket.
				synchronized (this) {
					runner.start();// The thread is paused by default.
					try {
						wait();// Wait for that thread to try and start the socket. If there's a failure, it'll
								// set the exception property *and then* return its lock.
					} catch (InterruptedException e) {// This shouldn't happen, but can. In case it does, this object
														// should be
														// considered dead.
						e.printStackTrace();
						return;
					}
				}
				IOException ex = excep;
				excep = null;// Clear exception for this attempt to launch the server.
				if (ex != null)
					eventManager.fire(ServerThreadSystemRestartFailureEvent.SERVER_THREAD_SYSTEM_RESTART_FAILURE_EVENT,
							new ServerThreadSystemRestartFailureEvent(CommunicationConnectionAcceptor.this, ex));
			}
		};
		ArlithRuntime.register(Instance.SERVER, thread);
		return thread;
	}

	public int getSpecifiedPort() {
		return port;
	}

	public int getActualPort() {
		return sock.getLocalPort();
	}

	public boolean isRunning() {
		return runner != null && runner.isAlive();
	}

	private ServerSocket sock;
	private Thread runner = getNewThread();

	public void setDaemon(boolean daemon) {
		if (runner.isAlive())
			throw new RuntimeException("Cannot set the daemon property of a running Server.");
		else
			runner.setDaemon(daemon);
	}

	public void start() throws IOException {
		if (isRunning())
			throw new RuntimeException("Cannot start a running server.");
		runner = getNewThread();

		// Lock on this object while starting the thread and opening the socket.
		synchronized (this) {
			runner.start();// The thread is paused by default.
			try {
				wait();// Wait for that thread to try and start the socket. If there's a failure, it'll
						// set the exception property *and then* return its lock.
			} catch (InterruptedException e) {// This shouldn't happen, but can. In case it does, this object should be
												// considered dead.
				e.printStackTrace();
				return;
			}
		}
		IOException ex = excep;
		excep = null;// Clear exception for this attempt to launch the server.
		if (ex != null)
			throw ex;// An error occurred during startup. Throw the exception here.
	}

	public void stop() throws IOException {
		sock.close();
	}

	/**
	 * By default, this method simply calls {@link #acceptConnection(Connection)} by
	 * wrapping the new, incoming connection in a {@link Communicator} and passing
	 * it to {@link #acceptConnection(Connection)}.
	 * 
	 * @param incomingSocketConnection
	 * @throws Exception
	 */
	protected void acceptSocket(Socket incomingSocketConnection) throws Exception {
		acceptConnection(new Communicator(incomingSocketConnection));
	}

	/**
	 * <p>
	 * (By default), called by {@link #acceptSocket(Socket)} with a new
	 * {@link Communicator} wrapping the {@link Socket}. This method is called
	 * whenever a new connection is received. It is meant to be implemented by
	 * subclasses to handle the new connection.
	 * </p>
	 * <p>
	 * If {@link #acceptOnNewThread} is set (it is <code>true</code> by default),
	 * then this method is called on an already-created, new thread. That thread can
	 * be accessed with {@link Thread#currentThread()}.
	 * </p>
	 * 
	 * @param communicator The newly accepted {@link Connection}.
	 * @throws Exception If any kind of error occurs. Such errors are handled
	 *                   gracefully by this class, and so will not interrupt the
	 *                   incoming-connection polling mechanism of this class. If an
	 *                   exception is thrown, a
	 *                   {@link ServerSocketAcceptFailureEvent} is fired with this
	 *                   connection acceptor, the exception, and the socket, as
	 *                   arguments.
	 */
	protected abstract void acceptConnection(Connection communicator) throws Exception;
}
