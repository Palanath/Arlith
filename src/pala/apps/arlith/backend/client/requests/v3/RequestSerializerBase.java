package pala.apps.arlith.backend.client.requests.v3;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v2.ConnectionStartupException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

public abstract class RequestSerializerBase implements RequestSerializer {

	/**
	 * <p>
	 * Used to store the current connection during operation. This field will be
	 * <code>null</code> if this {@link RequestSerializerBase} is stopped, and will
	 * be non-<code>null</code> so long as this {@link RequestSerializerBase} is
	 * started and in operation.
	 * </p>
	 * <p>
	 * If, during operation, the {@link CommunicationConnection} fails or is
	 * otherwise considered dead, it is replaced with a call to
	 * {@link #restartConnection()}, with a new, freshly {@link #prepareConnection()
	 * prepared} connection.
	 * </p>
	 */
	private CommunicationConnection connection;

	@Override
	public synchronized <R> R inquire(Inquiry<? extends R> inquiry) throws CommunicationProtocolError {
		if (connection == null)
			throw new IllegalStateException("Request Serializer is shut down and cannot perform requests.");
		return inquiry.inquire(connection);
	}

	@Override
	public void start() {
		if (connection != null)
			return;
		synchronized (this) {
			try {
				restartConnection();
			} catch (InterruptedException e) {
				return;// This may instead be changed to only stop if the interrupt() was due to
						// #stop() being called.
			}
		}
	}

	@Override
	public void stop() {
		if (connection == null)
			return;
		try {
			connection.close();
		} finally {
			connection = null;
		}
	}

	/**
	 * Called to (re)start the connection. This method is invoked whenever the
	 * {@link #connection} needs to be put into a state that it can be used for
	 * making requests. This method closes the {@link #connection} if it is
	 * currently non-<code>null</code> then invokes
	 * {@link #restartConnectionLoop()}.
	 * 
	 * @throws InterruptedException If {@link #stop()} is called or the thread on
	 *                              which this method executes is otherwise
	 *                              interrupted.
	 */
	private void restartConnection() throws InterruptedException {
		if (connection != null)
			connection.close();
		connection = restartConnectionLoop();
	}

	/**
	 * <h2>Overview</h2>
	 * <p>
	 * (Re)starts the {@link #connection} underlying this
	 * {@link RequestSerializerBase} so that it is ready to be used for requests.
	 * This method does not actually initialize a new connection (that is done by
	 * {@link #prepareConnection()}), this method defines logic for setting up a
	 * connection (repeatedly, in the event of failure to start up), and returns
	 * once the connection is ready to be used. It is called during operation of
	 * this {@link RequestSerializerBase} object to get another connection if the
	 * one currently in use fails. This method can determine how often a
	 * reconnection attempt is made when one fails and can handle errors occurring
	 * as a result of reconnection attempts that fail.
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
	 * and to catch any {@link CommunicationProtocolError}s,
	 * {@link RuntimeException}s, and {@link ConnectionStartupException}s.
	 * </p>
	 * <p>
	 * If no such errors occur, the connection is considered successfully opened,
	 * and so is returned. If any such errors occur, opening the connection is
	 * considered to have failed, and so the method logs the error to an appropriate
	 * output, sleeps for some wait time, and then loops.
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
	 * 	} catch (CommunicationProtocolError | RuntimeException | ConnectionStartupException e) {
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
	 * 	} catch (CommunicationProtocolError | RuntimeException | ConnectionStartupException e) {
	 * 		e.printStackTrace();
	 * 		Thread.sleep(i * 1000);
	 * 	}</code>
	 * </pre>
	 * 
	 * @return A successfully prepared {@link CommunicationConnection}.
	 */
	protected CommunicationConnection restartConnectionLoop() throws InterruptedException {
		for (int i = 2;; i += i >= 512 ? 1 : i)
			try {
				return prepareConnection();
			} catch (CommunicationProtocolError | RuntimeException | ConnectionStartupException e) {
				e.printStackTrace();
				Thread.sleep(i * 1000);
			}
	}

	protected abstract CommunicationConnection prepareConnection()
			throws CommunicationProtocolError, RuntimeException, ConnectionStartupException;

}
