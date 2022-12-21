package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.errors.ServerError;
import pala.apps.arlith.backend.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.communication.protocol.types.AuthTokenValue;
import pala.apps.arlith.backend.communication.protocol.types.HexHashValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

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
		return getPhoneNumber().getValue();
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

	public CreateAccountRequest(TextValue username, TextValue emailAddress, TextValue phoneNumber, HexHashValue password)
			throws CommunicationProtocolConstructionError {
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
	public AuthTokenValue receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, CreateAccountError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | CreateAccountError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
