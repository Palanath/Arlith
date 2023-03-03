package pala.apps.arlith.backend.client;

import pala.apps.arlith.backend.client.events.EventSubsystem;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

/**
 * <p>
 * Abstract base class for network classes used by the client. This class
 * manages a {@link Connection} which subclasses can access. If the
 * {@link Connection} fails for any reason, it can automatically be restarted by
 * the subclass through a call to {@link #restartConnection()}.
 * </p>
 * <p>
 * This class can either be in an idle or operating state. {@link #start()}ing
 * the {@link ClientNetworkingBase} puts it in the operating state and
 * {@link #stop()}ping it puts it in the idle state.
 * </p>
 * <p>
 * This class should be subclassed to maintain use of a {@link Connection} that
 * can be restarted automatically when it fails. When a subclass needs to use
 * the {@link Connection}, it should retrieve it with {@link #getConnection()},
 * wrap its use in a <code>try-catch</code> construct, catching any exceptions
 * that would deem the connection broken ({@link UnknownCommStateException}
 * &amp; {@link BlockException}) and calling {@link #restartConnection()} when
 * such exceptions occur. Commonly, subclasses will attempt the use of the
 * {@link Connection} in a loop, returning upon success or failing if the object
 * is shut down (i.e. if {@link #isRunning()} returns <code>false</code>).
 * </p>
 * <p>
 * This class supports being started back up again after being shut down.
 * </p>
 * <p>
 * Note that code which accesses the {@link #getConnection()} should be
 * synchronized. Care should be taken to assure that
 * {@link #restartConnection()} is not called by two threads at the same time.
 * Typically, subclasses are an API or utility class for the
 * {@link ArlithClient}, and will expose one or more methods that do something
 * with the {@link #getConnection() connection}. These methods are usually
 * synchronized against each other, including their checks for the lifelihood of
 * the {@link ClientNetworkingBase}, so that two threads do not invoke
 * {@link #restartConnection()} in sequence and nor at the same exact time.
 * </p>
 * 
 * @author Palanath
 *
 */
public abstract class ClientNetworkingBase {
	/**
	 * <p>
	 * Used to store the current connection during operation. This field will be
	 * <code>null</code> if this {@link ClientNetworkingBase} is stopped and will be
	 * non-<code>null</code> so long as the {@link ClientNetworkingBase} is started
	 * and in operation.
	 * </p>
	 * <p>
	 * If, during operation, the {@link Connection} fails or is otherwise considered
	 * dead, it is replaced with a call to {@link #restartConnection()}, with a new,
	 * freshly {@link #prepareConnection() prepared} connection.
	 * </p>
	 */
	private volatile Connection connection;
	private volatile Thread restartingThread;

	/**
	 * Instantiates a new {@link ClientNetworkingBase}.
	 */
	public ClientNetworkingBase() {
	}

	/**
	 * <p>
	 * Instantiates this {@link ClientNetworkingBase} into an already-started state.
	 * After this constructor is invoked, {@link #start()} need not be called before
	 * utilizing the {@link ClientNetworkingBase}'s {@link #getConnection()
	 * connection}. This constructor considers the provided {@link Connection} to be
	 * valid, as if successfully returned from {@link #prepareConnection()} in a
	 * call to {@link #restartConnection()}.
	 * </p>
	 * <p>
	 * The {@link ClientNetworkingBase} may still be {@link #stop() stopped} and
	 * {@link #start() started back up} again.
	 * </p>
	 * <p>
	 * This constructor is useful for facilities that wish to <i>fail</i> upon first
	 * attempt to connect, but wish to repeatedly retry thereafter. For example,
	 * Arlith's client's {@link EventSubsystem} is constructed in the
	 * {@link ArlithClientBuilder} with a {@link Connection} created by the
	 * {@link ArlithClientBuilder}, and if the {@link EventSubsystem} loses
	 * connection, it instantiates a new {@link Connection} and attempts to log in
	 * again. However, if the initial {@link Connection} fails, building the
	 * {@link ArlithClient} will fail.
	 * </p>
	 * 
	 * @param conn The {@link Connection} to start the {@link ClientNetworkingBase}
	 *             with.
	 */
	public ClientNetworkingBase(Connection conn) {
		connection = conn;
	}

	/**
	 * Determines whether this {@link ClientNetworkingBase} is currently running or
	 * not.
	 * 
	 * @return <code>true</code> if running, <code>false</code> otherwise.
	 */
	protected final boolean isRunning() {
		return connection != null;
	}

	/**
	 * Used by subclasses to retrieve the {@link Connection} for use.
	 * 
	 * @return The {@link Connection}, or <code>null</code> if this class is not
	 *         currently in operation (the {@link ClientNetworkingBase} is not
	 *         started).
	 */
	protected final Connection getConnection() {
		return connection;
	}

	/**
	 * <p>
	 * Starts this {@link ClientNetworkingBase}. This method puts the
	 * {@link ClientNetworkingBase} into a state of operation so that
	 * {@link #getConnection()} does not return <code>null</code>.
	 * </p>
	 */
	public synchronized void start() {
		if (isRunning())
			return;
		try {
			restartConnection();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * <p>
	 * Stops this {@link ClientNetworkingBase}. This method puts the
	 * {@link ClientNetworkingBase} into a state of being idle.
	 * </p>
	 */
	public synchronized void stop() {
		if (!isRunning())
			return;
		if (restartingThread != null)
			restartingThread.interrupt();
		try {
			connection.close();
		} finally {
			connection = null;
		}
	}

	/**
	 * <p>
	 * Called to (re)start the connection. This method is invoked whenever the
	 * {@link #connection} needs to be put into a state that it can be used. This
	 * method closes the {@link #connection}, if it is currently
	 * non-<code>null</code>, and then invokes {@link #restartConnectionLoop()}.
	 * </p>
	 * <p>
	 * Care should be taken to assure that (1) two threads do not execute this
	 * method a the same time (code should be synchronized) and that (2) two threads
	 * do not both determine the connection to be dead and call
	 * {@link #restartConnection()} in immediate sequence. Failing to assure (1) can
	 * result in faulty or undefined behavior, since this method explicitly expects
	 * to be executed by a maximum of one thread at a time, but failing to assure
	 * (2) is potentially resource wasteful. To assure (2), subclasses should
	 * generally make sure that {@link Thread}s running operations that have the
	 * potential to call {@link #restartConnection()} which check the state of the
	 * connection before using it will perform that check synchronized against other
	 * threads, so that two threads do not <i>both</i> determine that the connection
	 * is dead, and then both attempt to restart it, even if the calls to
	 * {@link #restartConnection()} are synchronized (as once the first thread
	 * finishes restarting the connection, the second thread will attempt to restart
	 * it since it, too, determined that the connection was deaad).
	 * </p>
	 * 
	 * @throws InterruptedException If {@link #stop()} is called, it interrupts the
	 *                              {@link Thread} currently executing this method.
	 *                              If
	 */
	protected final void restartConnection() throws InterruptedException {
		restartingThread = Thread.currentThread();
		if (connection != null)
			connection.close();
		connection = restartConnectionLoop();
		restartingThread = null;
	}

	/**
	 * <h2>Overview</h2>
	 * <p>
	 * (Re)starts the {@link #connection} underlying this
	 * {@link ClientNetworkingBase} so that it is ready to be used for requests.
	 * This method does not actually initialize a new connection (that is done by
	 * {@link #prepareConnection()}), this method defines logic for setting up a
	 * connection (repeatedly, in the event of failure to start up), and returns
	 * once the connection is ready to be used. It is called during operation of
	 * this {@link ClientNetworkingBase} object to get another connection if the one
	 * currently in use fails. This method can determine how often a reconnection
	 * attempt is made when one fails and can handle errors occurring as a result of
	 * reconnection attempts that fail.
	 * </p>
	 * <h2>Behavior</h2>
	 * <p>
	 * The standard implementation of this method is to loop and, inside the body of
	 * the loop, attempt to execute the code:
	 * </p>
	 * 
	 * <pre>
	 * <code>return prepareConnection();</code>
	 * </pre>
	 * 
	 * <p>
	 * If any {@link InterruptedException} happens during that execution, it is
	 * rethrown (as may indicate that a call to {@link #stop()} was made). If any
	 * other {@link Exception} occurs, it is logged (via
	 * {@link Exception#printStackTrace()}), the {@link Thread} is put to
	 * {@link Thread#sleep(long)}, and then the loop continues.
	 * </p>
	 * <h2>Examples</h2>
	 * <p>
	 * A very simple, constant-delay implementation of this method is as follows:
	 * </p>
	 * 
	 * <pre>
	 * <code>while (true)
	 * 	try {
	 * 		return prepareConnection();
	 * 	} catch (InterruptedException e) {
	 * 		throw e;
	 * 	} catch (Exception e) {
	 * 		e.printStackTrace();
	 * 		Thread.sleep(1000);
	 * 	}</code>
	 * </pre>
	 * <p>
	 * The default implementation of this method waits for 2 seconds upon the first
	 * failure, and then, upon subsequent failures, doubles until at 512 seconds of
	 * wait time, upon which it begins to increment the wait time by 1 barring
	 * subsequent successes:
	 * </p>
	 * 
	 * <pre>
	 * <code>for (int i = 2;; i += i >= 512 ? 1 : i) {
	 * 	try {
	 * 		return prepareConnection();
	 * 	} catch (InterruptedException e) {
	 * 		throw e;
	 * 	} catch (Exception e) {
	 * 		e.printStackTrace();
	 * 		Thread.sleep(i * 1000);
	 * 	}</code>
	 * </pre>
	 * 
	 * @return A successfully prepared {@link Connection}.
	 */
	protected Connection restartConnectionLoop() throws InterruptedException {
		for (int i = 2;; i += i >= 512 ? 1 : i)
			try {
				return prepareConnection();
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
				Thread.sleep(i * 1000);
			}
	}

	protected abstract Connection prepareConnection() throws InterruptedException, Exception;
}
