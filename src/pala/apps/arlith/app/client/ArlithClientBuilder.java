package pala.apps.arlith.app.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pala.apps.arlith.app.client.requests.v2.ConnectionStartupException;
import pala.apps.arlith.app.client.requests.v2.SingleThreadRequestSubsystem;
import pala.apps.arlith.backend.Utilities;
import pala.apps.arlith.backend.communication.authentication.AuthToken;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.communication.protocol.errors.LoginError;
import pala.apps.arlith.backend.communication.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.communication.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.backend.communication.protocol.requests.LoginRequest;
import pala.apps.arlith.backend.communication.protocol.types.HexHashValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.encryption.MalformedResponseException;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;

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

	public ArlithClientBuilder() throws UnknownHostException {
		this(null, "0", (InetAddress) null);
	}

	public ArlithClient login() throws LoginFailureException, LoginError, MalformedServerResponseException {
		CommunicationConnection conn = new CommunicationConnection();
		conn.setAddress(host);
		conn.setPort(port);

		// Perform login handshake
		try {
			conn.start();
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

			return new ArlithClient(conn, authToken, new RequestSubsystemImpl(host, port, authToken));
		} catch (Exception e) {
			conn.close();
			throw e;
		}
	}

	public ArlithClient createAccount() throws LoginFailureException, CreateAccountError {
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

			return new ArlithClient(conn, authToken, new RequestSubsystemImpl(host, port, authToken));
		} catch (Exception e) {
			conn.close();
			throw e;
		}
	}

	/**
	 * A default, fairly simple implementation of
	 * {@link SingleThreadRequestSubsystem}. This implementation is not highly
	 * configurable. Whenever the {@link SingleThreadRequestSubsystem} is restarted
	 * (and a new connection is opened), this implementation uses the
	 * {@link AuthToken} it was provided to create a new connection and log in to
	 * it, then returns that {@link CommunicationConnection}.
	 * 
	 * @author Palanath
	 *
	 */
	private static class RequestSubsystemImpl extends SingleThreadRequestSubsystem {

		private final InetAddress host;
		private final int port;
		private final AuthToken authToken;

		@Override
		protected CommunicationConnection prepareConnection()
				throws CommunicationProtocolError, RuntimeException, ConnectionStartupException {
			CommunicationConnection requestConnection = new CommunicationConnection() {
				// start() is called every time the connection is restarted, so we need to
				// authorize the connection when this happens.
				@Override
				public void start()
						throws IOException, InvalidKeyException, InvalidKeySpecException, IllegalBlockSizeException,
						BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException,
						InvalidAlgorithmParameterException, MalformedResponseException, SocketTimeoutException {
					super.start();// Start the connection.
					AuthRequest req = new AuthRequest(authToken);
					// Not yet documented, but this throws an exception when it fails. Otherwise, it
					// returns a completion.
					try {
						req.inquire(this);// Authorize the connection.
						// TODO Change from being "potentially recursive."
					} catch (CommunicationProtocolError e) {
						throw new RuntimeException(e);
					}
				}
			};
			requestConnection.setAddress(host);
			requestConnection.setPort(port);
			try {
				requestConnection.start();
			} catch (InvalidKeyException | InvalidKeySpecException | IllegalBlockSizeException | BadPaddingException
					| NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException
					| IOException | MalformedResponseException e) {
				throw new ConnectionStartupException(e);
			}

			return requestConnection;
		}

		private RequestSubsystemImpl(InetAddress host, int port, AuthToken authToken) {
			this.host = host;
			this.port = port;
			this.authToken = authToken;
		}
	}

}
