package pala.apps.arlith.backend.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.backend.client.requests.v2.StandardRequestSubsystem;
import pala.apps.arlith.backend.client.requests.v3.CancellableRequestQueueBase;
import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.backend.common.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.common.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.backend.common.protocol.requests.LoginRequest;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Communicator;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.networking.encryption.MalformedResponseException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

public class ArlithClientBuilder {

	public String getEmail() {
		return email;
	}

	public ArlithClientBuilder setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public ArlithClientBuilder setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public ArlithClientBuilder setUsername(String username) {
		this.username = username;
		return this;
	}

	public String getDiscriminant() {
		return discriminant;
	}

	public void setDiscriminant(String discriminant) {
		this.discriminant = discriminant;
	}

	public String getPassword() {
		return password;
	}

	public ArlithClientBuilder setPassword(String password) {
		this.password = password;
		return this;
	}

	public InetAddress getHost() {
		return host;
	}

	public ArlithClientBuilder setHost(InetAddress host) {
		this.host = host;
		return this;
	}

	public int getPort() {
		return port;
	}

	public ArlithClientBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public int getRequestConnections() {
		return requestConnections;
	}

	public ArlithClientBuilder setRequestConnections(int requestConnections) {
		this.requestConnections = requestConnections;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public ArlithClientBuilder setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public static final int DEFAULT_REQUEST_SYSTEM_DISTRIBUTION_COUNT = 3,
			DEFAULT_DESTINATION_PORT = Utilities.getPreferredPort(), DEFAULT_TIMEOUT = 2000;
	public static final String DEFAULT_DESTINATION_ADDRESS = Utilities.getPreferredDestinationAddress();

	private String username, password, email, phoneNumber, discriminant;
	private InetAddress host;
	private int port, requestConnections, timeout;

	public ArlithClientBuilder(String username, String password, InetAddress host, int port, int requestConnections,
			int timeout) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.requestConnections = requestConnections;
		this.timeout = timeout;
	}

	public ArlithClientBuilder(String username, String disc, String password, InetAddress host, int port,
			int requestConnections, int timeout) {
		this.username = username;
		discriminant = disc;
		this.password = password;
		this.host = host;
		this.port = port;
		this.requestConnections = requestConnections;
		this.timeout = timeout;
	}

	public ArlithClientBuilder(String username, String password) throws UnknownHostException {
		this(username, password, DEFAULT_DESTINATION_ADDRESS);
	}

	public ArlithClientBuilder(String username, String password, InetAddress host) {
		this(username, password, host, DEFAULT_DESTINATION_PORT);
	}

	public ArlithClientBuilder(String username, String password, String host) throws UnknownHostException {
		this(username, password, InetAddress.getByName(host));
	}

	public ArlithClientBuilder(String username, String password, InetAddress host, int port) {
		this(username, password, host, port, DEFAULT_TIMEOUT);
	}

	public ArlithClientBuilder(String username, String password, InetAddress host, int port, int timeout) {
		this(username, password, host, port, DEFAULT_REQUEST_SYSTEM_DISTRIBUTION_COUNT, timeout);
	}

	public ArlithClientBuilder(String username, String disc, String password, InetAddress host, int port, int timeout) {
		this(username, disc, password, host, port, DEFAULT_REQUEST_SYSTEM_DISTRIBUTION_COUNT, timeout);
	}

	public ArlithClientBuilder(String username, String password, String host, int port) throws UnknownHostException {
		this(username, password, InetAddress.getByName(host), port);
	}

	public ArlithClientBuilder(String username, String disc, String password, InetAddress host, int port) {
		this(username, disc, password, host, port, DEFAULT_TIMEOUT);
	}

	public ArlithClientBuilder(String username, String disc, String password, String host) throws UnknownHostException {
		this(username, disc, password, InetAddress.getByName(host));
	}

	public ArlithClientBuilder(String username, String disc, String password, String host, int port)
			throws UnknownHostException {
		this(username, disc, password, InetAddress.getByName(host), port);
	}

	public ArlithClientBuilder(String username, String disc, String password, InetAddress host) {
		this(username, disc, password, host, DEFAULT_DESTINATION_PORT);
	}

	public ArlithClientBuilder() {
		this(null, "0", (InetAddress) null);
	}

	public ArlithClient login() throws LoginFailureException, LoginError, MalformedServerResponseException,
			BlockException, UnknownCommStateException {
		Communicator conn;

		// Perform login handshake
		try {
			conn = new Communicator(new Socket(host, port));
		} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IOException
				| MalformedResponseException e) {
			throw new LoginFailureException(e);
		}

		try {
			LoginRequest loginRequest;
			loginRequest = email != null
					? LoginRequest.withEmail(new TextValue(email), HexHashValue.createAndHash(password))
					: phoneNumber != null
							? LoginRequest.withPhone(new TextValue(phoneNumber), HexHashValue.createAndHash(password))
							: LoginRequest.withUsername(new TextValue(username), new TextValue(discriminant),
									HexHashValue.createAndHash(password));
			AuthToken authToken;
			try {
				loginRequest.sendRequest(conn);
				authToken = loginRequest.receiveResponse(conn).getToken();
			} catch (IllegalArgumentException e) {
				throw new MalformedServerResponseException(
						"The server's response to a log-in request was not understandable.", e);
			} catch (LoginError e) {
				throw e;
			} catch (CommunicationProtocolError e) {
				throw new LoginFailureException(e);
			}

			InetAddress host = this.host;
			int port = this.port;

			StandardEventSubsystem es = new StandardEventSubsystem(conn, authToken) {

				@Override
				protected Socket prepareSocket() throws InterruptedException, Exception {
					return new Socket(host, port);
				}

			};
			RequestSubsystemImpl rs = new RequestSubsystemImpl(host, port, authToken);
			ArlithClient client = new ArlithClient(es, rs);
			es.setLogger(client.getLogger());
			rs.setLogger(client.getLogger());
			client.startup();
			return client;
		} catch (Exception e) {
			conn.close();
			throw e;
		}
	}

	public ArlithClient createAccount()
			throws LoginFailureException, CreateAccountError, BlockException, UnknownCommStateException {
		if (getEmail() == null)
			throw new RuntimeException("The email is required when creating an account.");
		CommunicationConnection conn = new CommunicationConnection();
		conn.setAddress(host);
		conn.setPort(port);

		// Perform account creation request
		try {
			conn.start();
		} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
				| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | IOException
				| MalformedResponseException e) {
			System.out.println("Host: " + host);
			System.out.println("Port: " + port);
			throw new LoginFailureException(e);
		}

		try {
			CreateAccountRequest createAccountRequest = new CreateAccountRequest(new TextValue(username),
					new TextValue(email), new TextValue(phoneNumber), HexHashValue.createAndHash(password));
			AuthToken authToken;
			try {
				createAccountRequest.sendRequest(conn);
				authToken = createAccountRequest.receiveResponse(conn).getToken();
			} catch (IllegalArgumentException e) {
				throw new MalformedServerResponseException(
						"The server's response to an attempt to create an account is not understandable.", e);
			} catch (CreateAccountError e) {
				throw e;
			} catch (CommunicationProtocolError e) {
				throw new LoginFailureException(e);
			}

			InetAddress host = this.host;
			int port = this.port;

			StandardEventSubsystem es = new StandardEventSubsystem(conn, authToken) {

				@Override
				protected Socket prepareSocket() throws InterruptedException, Exception {
					return new Socket(host, port);
				}

			};
			RequestSubsystemImpl rs = new RequestSubsystemImpl(host, port, authToken);
			ArlithClient client = new ArlithClient(es, rs);
			es.setLogger(client.getLogger());
			rs.setLogger(client.getLogger());
			client.startup();
			return client;
		} catch (Exception e) {
			conn.close();
			throw e;
		}
	}

	/**
	 * A default, fairly simple implementation of {@link StandardRequestSubsystem}.
	 * This implementation is not highly configurable. Whenever the
	 * {@link StandardRequestSubsystem} is restarted (and a new connection is
	 * opened), this implementation uses the {@link AuthToken} it was provided to
	 * create a new connection and log in to it, then returns that
	 * {@link CommunicationConnection}.
	 * 
	 * @author Palanath
	 *
	 */
	private static class RequestSubsystemImpl extends CancellableRequestQueueBase {

		private final InetAddress host;
		private final int port;
		private final AuthToken authToken;
		private Logger logger = Logger.STD;

		public Logger getLogger() {
			return logger;
		}

		public void setLogger(Logger logger) {
			this.logger = logger;
		}

		private RequestSubsystemImpl(InetAddress host, int port, AuthToken authToken) {
			this.host = host;
			this.port = port;
			this.authToken = authToken;
		}

		@Override
		protected Connection prepareConnection() throws InterruptedException, Exception {
			Communicator c = new Communicator(new Socket(host, port));
			AuthRequest ar = new AuthRequest(authToken);
			ar.inquire(c);
			return c;
		}
	}

}
