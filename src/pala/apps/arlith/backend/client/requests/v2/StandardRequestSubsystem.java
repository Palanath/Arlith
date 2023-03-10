package pala.apps.arlith.backend.client.requests.v2;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.requests.Action;
import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

public abstract class StandardRequestSubsystem implements RequestSubsystem {

	/**
	 * The current connection
	 */
	private CommunicationConnection connection;
	/**
	 * Thread used for queueing 
	 */
	private Thread queueThread;
	private final LinkedBlockingQueue<STRSAction<?>> queue = new LinkedBlockingQueue<>();

	/**
	 * The {@link Logger} used to log error and other messages to. This typically
	 * starts out as the {@link Arlith#getLogger() main Arlith Application Logger}
	 * and then gets changed to the {@link ArlithClient#getLogger() client's logger}
	 * once the client boots up and this {@link StandardRequestSubsystem} is
	 * attached to it.
	 */
	private Logger logger = Arlith.getLogger();

	/**
	 * Gets the {@link Logger} that this {@link StandardRequestSubsystem} uses. See
	 * {@link #logger} for more information.
	 * 
	 * @return The {@link Logger} used by this {@link StandardRequestSubsystem}
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Sets the {@link Logger} that this {@link StandardRequestSubsystem} uses. See
	 * {@link #logger} for more information.
	 * 
	 * @param logger The {@link Logger} used by this
	 *               {@link StandardRequestSubsystem}.
	 */
	public void setLogger(Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException("Logger should not be null.");
		this.logger = logger;
	}

	public StandardRequestSubsystem(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Instantiates this {@link StandardRequestSubsystem} with the
	 * {@link Arlith#getLogger() main Arlith Application Logger} as its
	 * {@link Logger}. Typical use of this object involves changing the
	 * {@link Logger} it uses to the {@link ArlithClient#getLogger() client's
	 * logger} once this object becomes attached to the client.
	 */
	public StandardRequestSubsystem() {
		this(Arlith.getLogger());
	}

	private abstract class STRSAction<R> implements ActionInterface<R> {

		private volatile R result;
		private volatile Exception exception;
		/**
		 * Boolean tracking whether this {@link STRSAction} has ever been queued for
		 * execution, by any thread. This is set when the {@link STRSAction} is executed
		 * immediately by a thread calling {@link #get()} (or a similar method) or when
		 * the {@link STRSAction} is queued to be executed via {@link #queue()}.
		 */
		private volatile boolean queued;
		private final Object monitor = new Object();

		@Override
		public RequestSubsystem getRequestSubsystem() {
			return StandardRequestSubsystem.this;
		}

		protected abstract R act(CommunicationConnection connection)
				throws CommunicationProtocolError, RuntimeException;

		/**
		 * Method called by the {@link StandardRequestSubsystem#queueThread} to actually
		 * execute an {@link Action}s in the queue so that the required state in the
		 * {@link STRSAction} gets set upon completion and waiting caller threads are
		 * notified. This method invokes {@link #act(CommunicationConnection)}, which
		 * does the actual logic behind this {@link STRSAction}, but also stores the
		 * result or exception, depending on whether this {@link STRSAction} completed
		 * normally or exceptionally, and notifies all threads currently blocked and
		 * waiting for a result from this {@link Action}.
		 * 
		 * @param connection The {@link CommunicationConnection} to execute the
		 *                   {@link Action} with.
		 */
		private void execute(CommunicationConnection connection) {
			try {
				// Attempt to complete the action. If we succeed, store the result in #result.
				result = act(connection);
			} catch (CommunicationProtocolError | RuntimeException e) {
				exception = e;
			}
		}

		/**
		 * <p>
		 * Executes a {@link STRSAction} in full, and handles any network errors
		 * prompting a restart of the underlying {@link #connection}. This method does
		 * not set the {@link STRSAction#queued} flag on the provided action, nor check
		 * it. This method does not synchronize over the action's monitor at all. Once
		 * completed, this {@link STRSAction} will have its {@link STRSAction#result} or
		 * its {@link STRSAction#exception} field set.
		 * </p>
		 * <p>
		 * This method is called by {@link StandardRequestSubsystem#queueThread} to
		 * execute an action that has been placed in the
		 * {@link StandardRequestSubsystem#queue}. Actions in the queue should already
		 * be marked as {@link #queued}.
		 * </p>
		 */
		private void execute() {
			execute(connection);
		}

		/**
		 * <p>
		 * Runs an unqueued action, in full, on the current thread, synchronizing over
		 * the action's monitor and setting the {@link #queued} flag. If the action has
		 * already been {@link #queued}, this method releases its synchronization lock
		 * on the action's monitor and does not execute the action.
		 * </p>
		 */
		private void runAction() {
			synchronized (this.monitor) {
				if (!this.queued) {
					this.queued = true;
					synchronized (StandardRequestSubsystem.this) {
						execute();
					}
					// Other types of exceptions do not need to be passed to this thread. As a
					// matter of fact, all other RuntimeExceptions are handled by the
					// STRSAction#execute method that we called.
				}
			}
		}

		/*
		 * All four types of exceptions are propagated to the caller, although
		 * UnknownCommStateExceptions and BlockExceptions are ALSO sent to the #thread
		 * so that it knows to restart the connection. No "retry" attempts are made.
		 * This may need to be fixed in the future.
		 */

		@Override
		public R get() throws CommunicationProtocolError, RuntimeException {
			runAction();// Run the action on this thread. Perform all synchronization and everything
						// necessary.
			// Handle the result.
			if (exception != null)
				if (exception instanceof CommunicationProtocolError)
					throw (CommunicationProtocolError) exception;
				else
					throw (RuntimeException) exception;
			else
				return result;
		}

		@Override
		public void queue() {
			// Queuing is simple; all we have to do is check if the action is already
			// queued, and if it isn't, queue it.
			synchronized (monitor) {
				if (!queued) {
					queue.add(this);
					queued = true;
				}
			}
		}

		@Override
		public <N> ActionInterface<N> then(Function<? super R, ? extends N> converter) {
			return new STRSAction<N>() {

				@Override
				protected N act(CommunicationConnection connection)
						throws CommunicationProtocolError, RuntimeException {
					return converter.apply(STRSAction.this.act(connection));
				}
			};
		}

		@Override
		public <N> ActionInterface<N> thenInquire(Function<? super R, ? extends Inquiry<N>> inquiryGenerator) {
			return new STRSAction<N>() {

				@Override
				protected N act(CommunicationConnection connection)
						throws CommunicationProtocolError, RuntimeException {
					return inquiryGenerator.apply(STRSAction.this.act(connection)).inquire(connection);
				}
			};
		}

		@Override
		public R poll() {
			return result;
		}

		@Override
		public Exception getException() {
			// See #get for more comments on generally the same code.
			runAction();
			return exception;
		}

		@Override
		public Exception pollException() {
			return exception;
		}

		@Override
		public ActionInterface<R> handle(Function<? super Exception, ? extends R> exceptionHandler) {
			return new STRSAction<R>() {

				@Override
				protected R act(CommunicationConnection connection)
						throws CommunicationProtocolError, RuntimeException {
					try {
						return STRSAction.this.act(connection);
					} catch (Exception e) {
						return exceptionHandler.apply(e);
					}
				}
			};
		}

	}

	/**
	 * <p>
	 * Stops this {@link StandardRequestSubsystem} and resets its state so that it
	 * is ready to be started up again (if desired) or discarded. If this
	 * {@link StandardRequestSubsystem} is not yet started or is in a discarded
	 * state, this method does nothing. This method will interrupt any active
	 * requests waiting to be executed on the queue thread, and may even interrupt
	 * actions mid-execution.
	 * </p>
	 * <p>
	 * Specifically, this method closes the {@link #connection} then interrupts the
	 * {@link #queueThread}, then sets both to <code>null</code>. It then clears the
	 * {@link #queue} of {@link STRSAction}s that have been submitted to this
	 * {@link StandardRequestSubsystem}.
	 * </p>
	 */
	public void stop() {
		if (connection == null)
			return;
		try {
			connection.close();
			queueThread.interrupt();
		} finally {
			connection = null;
			queueThread = null;
		}
	}

	/**
	 * <p>
	 * Starts this {@link StandardRequestSubsystem} by creating a new {@link Thread}
	 * and a new {@link CommunicationConnection} using {@link #prepareConnection()},
	 * then having the thread wait for new {@link STRSAction}s to execute. The
	 * {@link CommunicationConnection} is connected to the server and prepared for
	 * use once this method is called, and is kept alive (or restarted, as
	 * appropriate), before, and throughout, the use of this
	 * {@link StandardRequestSubsystem}. It is shut down upon a call to
	 * {@link #stop()}.
	 * </p>
	 * <p>
	 * If the connection fails at any point during operation, for one reason or
	 * another, it is restarted (specifically, another call to
	 * {@link #prepareConnection()} is made to build a fresh, new connection that is
	 * ready to be used).
	 * </p>
	 */
	public void start() {
		queueThread = new Thread() {
			@Override
			public void run() {

				synchronized (StandardRequestSubsystem.this) {
					try {
						restartConnection();
					} catch (InterruptedException e1) {
						return;
					}
				}
				// Continuously loop (while we have not been terminated) to handle the next
				// action submitted to this subsystem. If there is no next action, queue.take()
				// will block until there is.
				while (true && !Thread.interrupted()) {
					STRSAction<?> c;
					try {
						c = queue.take();
					} catch (InterruptedException e) {
						// If this thread is interrupted, the #stop method must have been called. We
						// should therefore return. (Any items in the queue are cleared by the #stop
						// method.)
						return;
					}

					synchronized (StandardRequestSubsystem.this) {
						c.execute();
					}
				}
			}
		};
		queueThread.setDaemon(true);
		queueThread.start();
	}

	/**
	 * <p>
	 * Creates a new {@link CommunicationConnection} and prepares it, so that
	 * {@link Inquiry Inquiries} are ready to be sent on it. This method is
	 * responsible for instantiating {@link CommunicationConnection}s, connecting
	 * them to a server, <i>and potentially</i> for logging into the server on them,
	 * so that nothing remains to be done before {@link Inquiry Inquiries} can be
	 * made on them. This method is called whenever a
	 * {@link StandardRequestSubsystem} is started and whenever it encounters an
	 * error with the server, prompting a restart in the connection.
	 * </p>
	 * 
	 * @return A new, prepared {@link CommunicationConnection}.
	 * @throws ConnectionStartupException In case the actual network connection
	 *                                    could not be started.
	 */
	protected abstract CommunicationConnection prepareConnection()
			throws CommunicationProtocolError, RuntimeException, ConnectionStartupException;

	private void restartConnection() throws InterruptedException {
		if (connection != null)
			connection.close();
		for (int i = 2;; i += i >= 512 ? 1 : i)
			try {
				connection = prepareConnection();// Restarts *probably* should not be made for
													// CommunicationProtocolErrors, since those
													// are indicative of the server sending a response that this client
													// could not understand, in which case, retrying the same thing is
													// *likely* to give the same exact, illegible response.
				// The exception types' documentation describe what they are for.
				// RuntimeExceptions are general purpose.
				break;
			} catch (CommunicationProtocolError | RuntimeException | ConnectionStartupException e) {
				logger.err(
						"An error occurred while trying to restart the request subsystem connection to the server. Retrying in "
								+ i + " seconds.");
				logger.err(e);
				Thread.sleep(i * 1000);
			}
	}

	@Override
	public <R> ActionInterface<R> action(Inquiry<? extends R> inquiry) {
		return new STRSAction<R>() {
			@Override
			protected R act(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException {
				return inquiry.inquire(connection);
			}
		};
	}

	@Override
	public <R> ActionInterface<R> executable(ArlithFunction<? extends R> executable) {
		return new STRSAction<R>() {
			@Override
			protected R act(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException {
				return executable.execute(connection);
			}
		};
	}

}
