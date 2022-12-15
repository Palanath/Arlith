package pala.apps.arlith.api.communication.authentication;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import pala.apps.arlith.api.communication.protocol.requests.CreateAccountRequest;
import pala.apps.arlith.api.communication.protocol.requests.LoginRequest;
import pala.apps.arlith.api.communication.protocol.types.AuthTokenValue;
import pala.libs.generic.strings.StringTools;

/**
 * <p>
 * An {@link AuthToken Authentication Token} is a small string of bytes that
 * represents a unique and secure <i>key</i> with which a user may quickly
 * identify itself to, and log into, a server. An {@link AuthToken} object
 * contains {@value #AUTH_TOKEN_SIZE} securely randomly generated bytes. There
 * is an {@link AuthTokenValue} used for sending {@link AuthToken}s between the
 * client and the server.
 * </p>
 * <h1>Purpose</h1>
 * <p>
 * {@link AuthToken}s are issued by the server to the client when the client
 * {@link CreateAccountRequest creates an account} or {@link LoginRequest logs
 * in} to the server. More specifically, the server responds to the client's
 * request to create an account or log in with an {@link AuthTokenValue}, which
 * the client stores while it is connected. Once the client creates an account
 * or logs in, it uses the open connection it established with the server for
 * requests, (see [TODO reference to information on how the log in connection
 * becomes the request connection] for more details), and then it opens another
 * connection, for event handling, and connects to the server, authenticating
 * itself (proving its identity, over the new connection, to the server) with
 * the {@link AuthToken} it was given by the server. This prevents the client
 * from having to use/store passwords to log in, and allows the server to cause
 * a client to "become logged out" by invaldating the auth token, (usually after
 * some long period of time has passed without attempts to log in, or upon user
 * request), if need be. The client also uses {@link AuthToken}s for restoring
 * connections that drop; after one of its connections fails, it opens a new one
 * and authenticates itself using the {@link AuthToken} it has kept stored.
 * <h1>API</h1>
 * <p>
 * {@link AuthToken} objects are immutable. This class has various factory
 * functions each of whose name is prefixed with <code>from</code>, and some
 * format methods, which return this {@link AuthToken} as a {@link String} in
 * various formats.
 * </p>
 * <p>
 * This class also allows for generation of new {@link AuthToken}s either
 * through {@link #newAuthToken()} or the {@link AuthToken} constructor. Both of
 * these means will initialize the {@link AuthToken} with securely randomly
 * generated bytes. See {@link #AuthToken()} for the implementation of this.
 * </p>
 * <p>
 * This class supports the following <b>formats</b> that {@link AuthToken}s can
 * be formatted as and parsed from:
 * </p>
 * <style> table, th, td { border: solid 1px currentcolor; border-collapse:
 * collapse; } th, td { padding: 0.3em; } </style>
 * <table summary="Lists formats supported by this class as well as methods for
 * converting to and from the formats, with a small description about each
 * format.">
 * <caption>Supported Formats</caption>
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * <th><code>From</code> Function</th>
 * <th><code>Get</code> Function</th>
 * </tr>
 * <tr>
 * <td>Hexadecimal</td>
 * <td>A hexadecimal representation of all the bytes in this {@link AuthToken}.
 * Each byte is converted into two hexadecimal characters in the same order they
 * are stored in this {@link AuthToken}'s internal {@link #bytes} byte
 * array.</td>
 * <td>{@link #fromHex(String)}</td>
 * <td>{@link #getHex()}</td>
 * </tr>
 * <tr>
 * <td>Numeric</td>
 * <td>Outputs a numeric representation of this {@link AuthToken}. See the
 * method for implementation/format details.</td>
 * <td>{@link #fromNumber(String)}</td>
 * <td>{@link #getNumber()}</td>
 * </tr>
 * <tr>
 * <td>String</td>
 * <td>Outputs a textual representation of this {@link AuthToken}. This may be
 * the same as {@link #getHex()} or {@link #getNumber()}, or it may be
 * different. It may also change. Regardless of what the format is, it will
 * always be consistent with the respective {@link #fromString(String)}
 * function.</td>
 * <td>{@link #fromString(String)}</td>
 * <td>{@link #toString()}</td>
 * </tr>
 * </table>
 * <h1>Class Structure</h1>
 * <p>
 * Each {@link AuthToken} instance stores a <code>byte</code> array containing
 * all of the data in the token. {@link AuthToken}s can be printed to strings
 * either in <i>hexadecimal format</i> (via {@link #getHex()}), <i>numerical
 * format</i> (via {@link #getNumber()}), or <i>string format</i> (via
 * {@link #toString()}). Likewise, {@link AuthToken} objects can be created from
 * {@link AuthToken} strings using {@link #fromHex(String)},
 * {@link #fromNumber(String)}, and {@link #fromString(String)}. <b>The string
 * format</b> is subject to change, but regardless of which implementation,
 * {@link #fromString(String)} must be able to parse strings created with
 * {@link #toString()} on the same implementation.
 * </p>
 * <p>
 * The {@link AuthToken} class keeps track of an {@link #AUTH_TOKEN_SIZE}
 * constant, which can be easily modified to change the size of
 * {@link AuthToken}s before compilation (if in the future, they ever need to be
 * grown or shrunk in size).
 * </p>
 * 
 * 
 * @author Palanath
 *
 */
public class AuthToken {

	private static final int AUTH_TOKEN_SIZE = 128;

	private final byte[] bytes;

	public AuthToken() {
		new SecureRandom().nextBytes(bytes = new byte[AUTH_TOKEN_SIZE]);
	}

	public AuthToken(byte[] bytes) {
		if (bytes.length != AUTH_TOKEN_SIZE)
			throw new IllegalArgumentException("AuthTokens must have a length of " + AUTH_TOKEN_SIZE + " bytes.");
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	public String getHex() {
		return StringTools.toHexString(bytes);
	}

	public String getNumber() {
		byte[] b = new byte[AUTH_TOKEN_SIZE + 1];
		System.arraycopy(bytes, 0, b, 1, AUTH_TOKEN_SIZE);
		b[0] = 0x7F;
		return new BigInteger(b).toString();
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(bytes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return Arrays.equals(bytes, ((AuthToken) obj).bytes);
	}

	/**
	 * Calls and returns {@link #getHex()}.
	 */
	@Override
	public String toString() {
		return getHex();
	}

	public static AuthToken newAuthToken() {
		return new AuthToken();
	}

	public static AuthToken fromHex(String hex) throws NumberFormatException {
		return new AuthToken(StringTools.fromHexString(hex));
	}

	public static AuthToken fromNumber(String number) {
		return fromNumber(new BigInteger(number));
	}

	public static AuthToken fromBytes(byte... bytes) {
		return new AuthToken(Arrays.copyOf(bytes, bytes.length));
	}

	private static AuthToken fromNumber(BigInteger number) {
		return new AuthToken(Arrays.copyOfRange(number.toByteArray(), 1, AUTH_TOKEN_SIZE + 1));
	}

	/**
	 * Returns an {@link AuthToken} given its {@link String} representation. This
	 * method serves as a counterpart to {@link #toString()}.
	 * 
	 * @param authToken The {@link String} representation of the {@link AuthToken}
	 *                  to use.
	 * @return A newly created {@link AuthToken} object based off of the given auth
	 *         token {@link String}.
	 * @author Palanath
	 */
	public static AuthToken fromString(String authToken) {
		return fromHex(authToken);
	}
}
