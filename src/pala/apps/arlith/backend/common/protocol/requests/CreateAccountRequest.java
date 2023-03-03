package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.common.protocol.types.HexHashValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <h1>Create Account Request</h1>
 * <p>
 * Represents a request to create an account. After successful invocation of
 * this request, a new account with the specified {@link #username},
 * {@link #emailAddress}, {@link #phoneNumber}, and {@link #password} will be
 * created, the connection on which the request was made will be authorized as
 * the created account, and a newly generated {@link AuthTokenValue} containing
 * the {@link AuthToken} assigned to the account will have been returned.
 * </p>
 * <p>
 * This request requires the connection to be
 * <em class="unauthorized">unauthorized</em>.
 * </p>
 * <h2>Request Structure</h2>
 * <p>
 * This request has the following parameters:
 * </p>
 * <table title="Parameters" class="parameters">
 * <tr>
 * <th>Parameter Name</th>
 * <th>Type</th>
 * <th>Description</th>
 * <th>Optional?</th>
 * </tr>
 * <tr>
 * <td>Username</td>
 * <td>{@link TextValue}</td>
 * <td>The account's username.</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Email Address</td>
 * <td>{@link TextValue}</td>
 * <td>The new email to associate with the account.</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>Phone Number</td>
 * <td>{@link TextValue}</td>
 * <td>The new phone number to associate with the account.</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>Password</td>
 * <td>{@link HexHashValue}</td>
 * <td>A hash of the password to associate with the account.</td>
 * <td>No</td>
 * </tr>
 * </table>
 * <p>
 * This request can result in the following errors:
 * </p>
 * <table class="errors" title="Errors">
 * <tr>
 * <th>Error Name</th>
 * <th>Formal Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Syntax</td>
 * <td>{@link SyntaxError}</td>
 * <td>If the request itself is syntactically malformed.</td>
 * </tr>
 * <tr>
 * <td>Rate Limit</td>
 * <td>{@link RateLimitError}</td>
 * <td>If the server attempts to rate limit the connection.</td>
 * </tr>
 * <tr>
 * <td>Server</td>
 * <td>{@link ServerError}</td>
 * <td>If the server encounters an unknown, unexpected error.</td>
 * </tr>
 * <tr>
 * <td>Restricted</td>
 * <td>{@link RestrictedError}</td>
 * <td>If the current connection does not have permission to invoke this
 * request.</td>
 * </tr>
 * <td>Create Account</td>
 * <td>{@link CreateAccountError}</td>
 * <td>If any parameters violate <i>uniqueness</i> or <i>syntax</i>. See below
 * <sup>[1]</sup> for details.</td>
 * </tr>
 * </table>
 * <p>
 * <sup>[1]</sup>Arguments must follow <i>syntactic</i> restrictions and
 * <i>uniqueness</i> restrictions, where applicable. Syntax restrictions regard
 * the format and composition of the argument and uniqueness restrictions assert
 * that certain account data is unique per-account (and can thus, be used to
 * uniquely identify an account). For example, usernames are syntactically
 * restricted from containing <code>#</code> characters. Another example is that
 * no two accounts can have the same email address. These restrictions can be
 * found amongst general documentation for user account data at <a href=
 * "https://arlith.net/user-accounts/">https://arlith.net/user-accounts/</a>.
 * </p>
 * <p>
 * The following arguments are subject to syntax restrictions:
 * </p>
 * <ul>
 * <li>Usernames</li>
 * <li>Email Addresses</li>
 * <li>Phone Numbers</li>
 * </ul>
 * <p>
 * The following arguments are subject to uniqueness restrictions:
 * </p>
 * <ul>
 * <li>Email Addresses</li>
 * <li>Phone Numbers</li>
 * </ul>
 * <h2>Response</h2>
 * <p>
 * If the request is successful, the response value is a freshly issued
 * {@link AuthToken} which can be used to log in to Arlith again. If the request
 * is unsuccessful due to a uniqueness or syntax violation, a
 * {@link CreateAccountError} will be returned. The error's
 * {@link CreateAccountError#getErrorType() error type} property will indicate
 * the <i>argument</i> which caused the request to fail as well as whether
 * uniqueness was violated (if the given argument was already in use
 * (<i>taken</i>) by another account) or if a syntax violation occurred (i.e.,
 * an <i>illegal</i> argument was provided).
 * </p>
 * 
 * @author Palanath
 *
 */
public class CreateAccountRequest extends SimpleCommunicationProtocolRequest<AuthTokenValue> {

	public void setUsername(TextValue username) {
		this.username = username;
	}

	public void setEmailAddress(TextValue emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setPhoneNumber(TextValue phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void clearPhoneNumber() {
		setPhoneNumber(null);
	}

	public void setPassword(HexHashValue password) {
		this.password = password;
	}

	public final static String REQUEST_NAME = "create-account";
	private final static String USERNAME_KEY = "username", PASSWORD_KEY = "password",
			EMAIL_ADDRESS_KEY = "email-address", PHONE_NUMBER_KEY = "phone-number";

	private TextValue username, emailAddress, phoneNumber;
	private HexHashValue password;

	public TextValue getUsername() {
		return username;
	}

	public String username() {
		return getUsername().getValue();
	}

	public String emailAddress() {
		return getEmailAddress().getValue();
	}

	public String phoneNumber() {
		return hasPhoneNumber() ? getPhoneNumber().getValue() : null;
	}

	public TextValue getEmailAddress() {
		return emailAddress;
	}

	public HexHashValue getPassword() {
		return password;
	}

	public TextValue getPhoneNumber() {
		return phoneNumber;
	}

	public boolean hasPhoneNumber() {
		return phoneNumber != null;
	}

	public CreateAccountRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		username = new TextValue(properties.get(USERNAME_KEY));
		password = new HexHashValue(properties.get(PASSWORD_KEY));
		emailAddress = new TextValue(properties.get(EMAIL_ADDRESS_KEY));
		if (properties.containsKey(PHONE_NUMBER_KEY))
			phoneNumber = new TextValue(properties.get(PHONE_NUMBER_KEY));
	}

	public CreateAccountRequest(TextValue username, TextValue emailAddress, TextValue phoneNumber,
			HexHashValue password) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.username = username;
		this.emailAddress = emailAddress;
		this.phoneNumber = phoneNumber;
		this.password = password;
	}

	public CreateAccountRequest(TextValue username, TextValue emailAddress, HexHashValue password)
			throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.username = username;
		this.emailAddress = emailAddress;
		this.password = password;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USERNAME_KEY, username.json());
		object.put(PASSWORD_KEY, password.json());
		object.put(EMAIL_ADDRESS_KEY, emailAddress.json());
		if (phoneNumber != null)
			object.put(PHONE_NUMBER_KEY, phoneNumber.json());
	}

	@Override
	protected AuthTokenValue parseReturnValue(JSONValue json) {
		return new AuthTokenValue(json);
	}

	@Override
	public AuthTokenValue receiveResponse(Connection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, CreateAccountError,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | CreateAccountError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
