package pala.apps.arlith.backend.connections.scp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.backend.connections.encryption.MalformedResponseException;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.Communicator;
import pala.apps.arlith.backend.connections.networking.Connection;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.backend.streams.InputStream;
import pala.apps.arlith.backend.streams.OutputStream;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventSystem;
import pala.libs.generic.json.JSONValue;

public class CommunicationConnection implements EventSystem<ClientEvent>, Connection {

	// TODO Add reading method that takes a max size param.

	/**
	 * Represents an atomic communication with the server. The endpoint will not
	 * undergo side-effects as a result of the reception of any less than one whole
	 * {@link AtomicCommunication}.
	 * 
	 * @author Palanath
	 *
	 * @param <O> The result of the {@link AtomicCommunication}.
	 */
	private interface AtomicCommunication<O> {
		O act(Communicator c) throws UnknownCommStateException, BlockException;
	}

	/**
	 * Represents an atomic network operation (i.e. one that can be restarted if it
	 * fails) that does not return a result to the caller. <b>These do not
	 * necessarily have to write data to the connection.</b> For example,
	 * {@link CommunicationConnection#readVariableBlock(OutputStream)} is an
	 * {@link AtomicWrite} operation.
	 * 
	 * @author Palanath
	 *
	 */
	private interface AtomicWrite extends AtomicCommunication<Void> {
		void write(Connection c) throws UnknownCommStateException, BlockException;

		@Override
		default Void act(Communicator c) throws UnknownCommStateException, BlockException {
			write(c);
			return null;
		}
	}

	protected final <O> O execute(AtomicCommunication<O> act) throws BlockException, UnknownCommStateException {
		try {
			return act.act(communicator);
		} catch (UnknownCommStateException e) {
			if (sock == null)
				throw new RuntimeException("Connection manually stopped.");
			throw e;
		}
	}

	/**
	 * <p>
	 * Runs the atomic communication operation and handles any network problems
	 * (i.e. an {@link UnknownCommStateException} or a {@link BlockException}).
	 * {@link CommunicationConnection}s are expected to restart their underlying
	 * socket connection in response to an {@link UnknownCommStateException} or
	 * {@link BlockException}, and then to retry the {@link AtomicCommunication}
	 * operation that resulted in one of those two exceptions, repeatedly, in a way
	 * that subclassing code sees fit.
	 * </p>
	 * <p>
	 * The default behavior of this method logs a generic connection message, sleeps
	 * for 5 seconds, then calls {@link #restartConnectionOnError()}. Once that
	 * method returns, this method retries the {@link AtomicCommunication}
	 * operation. This is done until the {@link AtomicCommunication} operation
	 * succeeds, in which case its return value is returned.
	 * </p>
	 * 
	 * @param <O> The return type of the {@link AtomicCommunication} operation.
	 * @param act The {@link AtomicCommunication} operation to perform.
	 * @return The result of the {@link AtomicCommunication} operation.
	 * @throws RuntimeException If the {@link CommunicationConnection} is
	 *                          interrupted while executing.
	 */
	protected <O> O act(AtomicCommunication<O> act) throws RuntimeException {
		while (true)
			try {
				return execute(act);
			} catch (BlockException e) {
				Logging.err("Connection broken; waiting 5 seconds and restarting.");
				try {
					Thread.sleep(5000);
					restartConnectionOnError();
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			} catch (UnknownCommStateException e) {
				if (sock == null)
					throw new RuntimeException("Connection manually stopped.");
				Logging.err("Connection broken; waiting 5 seconds and restarting.");
				try {
					Thread.sleep(5000);
					restartConnectionOnError();
				} catch (InterruptedException e1) {
					throw new RuntimeException(e1);
				}
			}
	}

	private void write(AtomicWrite write) {
		act((AtomicCommunication<?>) write);
	}

	/**
	 * Restarts the connection in a loop until it can be established. A 5 second
	 * delay is made between connection attempts.
	 * 
	 * @throws InterruptedException If the thread is interrupted while the sleep is
	 *                              in progress.
	 */
	protected void restartConnectionOnError() throws InterruptedException {
		while (true)
			try {
				Thread.sleep(5000);
				start();
				break;
			} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
					| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
					| MalformedResponseException e) {
				Logging.err("An error occurred while restarting a connection to the server. Retrying in 5 seconds.");
				Logging.err(e);
			} catch (IOException e) {
				Logging.err("Failed to establish connection with server. Retrying in 5 seconds");
				Logging.err(e);
			}
	}

	public void writeVariableBlock(InputStream is) {
		write((AtomicWrite) a -> a.writeVariableBlock(is));
	}

	public void writeVariableBlock(byte[] b) {
		write(a -> a.writeVariableBlock(b));
	}

	public void readVariableBlock(OutputStream acceptor) {
		write(a -> a.readVariableBlock(acceptor));
	}

	public byte[] readBlockShort() {
		return act(Communicator::readBlockShort);
	}

	public byte[] readBlockShort(short maxLen) {
		return act(a -> a.readBlockShort(maxLen));
	}

	public byte[] readBlockLong() {
		return act(Communicator::readBlockLong);
	}

	public byte[] readBlockLong(int maxLen) {
		return act(a -> a.readBlockLong(maxLen));
	}

	public void writeBlockShort(byte[] b) {
		write(a -> a.writeBlockShort(b));
	}

	public void writeBlock(byte[] b) {
		write(a -> a.writeBlock(b));
	}

	public void sendStringShort(String s) {
		write(a -> a.sendStringShort(s));
	}

	public void sendString(String s) {
		write(a -> a.sendString(s));
	}

	public String readStringShort() {
		return act(Communicator::readStringShort);
	}

	public String readStringShort(int lim) {
		return act(a -> a.readStringShort(lim));
	}

	public String readString() {
		return act(Communicator::readString);
	}

	public String readString(int lim) {
		return act(a -> a.readString(lim));
	}

	public void sendJSON(JSONValue value) {
		write(a -> a.sendJSON(value));
	}

	public JSONValue readJSON() {
		return act(Communicator::readJSON);
	}

	public JSONValue readJSON(int lim) {
		return act(a -> a.readJSON(lim));
	}

	private final EventManager<ClientEvent> eventManager = new EventManager<>();

	private InetAddress address;
	private int port, timeout;

	private Communicator communicator;

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout for an attempt to connect in milliseconds.
	 * 
	 * @param timeout The timeout in milliseconds.
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private Socket sock;

	public boolean isRunning() {
		return sock != null;
	}

	/**
	 * Starts up this {@link CommunicationConnection}, {@link #close() closing} it
	 * first if it's already open.
	 * 
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws MalformedResponseException
	 * @throws SocketTimeoutException
	 * @author Palanath
	 */
	public void start() throws IOException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException,
			MalformedResponseException, SocketTimeoutException {
		if (sock != null)
			close();
		try {
			(sock = new Socket()).connect(new InetSocketAddress(address, port), timeout);
			communicator = new Communicator(sock) {
				@Override
				public void close() {
					CommunicationConnection.this.close();
				}
			};
		} catch (IOException | InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException
				| BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | MalformedResponseException e) {
			close();
			throw e;
		}
	}

	public void close() {
		Socket s = sock;
		sock = null;
		communicator = null;
		if (s != null)
			try {
				s.close();
			} catch (IOException e) {
				eventManager.fire(ClientCloseFailureEvent.CLIENT_CLOSE_FAILURE_EVENT,
						new ClientCloseFailureEvent(this, e));
			}
	}

	@Override
	public EventManager<ClientEvent> getEventManager() {
		return eventManager;
	}

}
