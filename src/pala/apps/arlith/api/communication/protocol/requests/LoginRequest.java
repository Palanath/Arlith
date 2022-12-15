package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.LoginError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.errors.TFARequiredError;
import pala.apps.arlith.api.communication.protocol.types.AuthTokenValue;
import pala.apps.arlith.api.communication.protocol.types.HexHashValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class LoginRequest extends SimpleCommunicationProtocolRequest<AuthTokenValue> {
	public static final String REQUEST_NAME = "login";

	private static final String PASSWORD_KEY = "password", USERNAME_KEY = "username", EMAIL_KEY = "email",
			PHONE_KEY = "phone", DISC_KEY = "disc";

	private HexHashValue password;
	private TextValue username, email, phone, disc;

	public LoginRequest(TextValue username, TextValue disc, HexHashValue password) {
		super(REQUEST_NAME);
		this.password = password;
		this.username = username;
		this.disc = disc;
	}

	private LoginRequest(HexHashValue passwordHash) {
		super(REQUEST_NAME);
		password = passwordHash;
	}

	public LoginRequest() {
		super(REQUEST_NAME);
	}

	public LoginRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		password = new HexHashValue(properties.get(PASSWORD_KEY));
		if (properties.containsKey(USERNAME_KEY)) {
			username = new TextValue(properties.get(USERNAME_KEY));
			disc = new TextValue(properties.get(DISC_KEY));
		} else if (properties.containsKey(PHONE_KEY)) {
			phone = new TextValue(properties.get(PHONE_KEY));
		} else if (properties.containsKey(EMAIL_KEY))
			email = new TextValue(properties.get(EMAIL_KEY));
	}

	public HexHashValue getPassword() {
		return password;
	}

	public TextValue getUsername() {
		return username;
	}

	public TextValue getEmail() {
		return email;
	}

	public void setEmail(TextValue email) {
		this.email = email;
		phone = null;
		username = null;
		disc = null;
	}

	public TextValue getPhone() {
		return phone;
	}

	public void setPhone(TextValue phone) {
		this.phone = phone;
		email = null;
		username = null;
		disc = null;
	}

	public void setUsername(TextValue username) {
		this.username = username;
		email = null;
		phone = null;
	}

	public void setDisc(TextValue disc) {
		this.disc = disc;
		email = null;
		phone = null;
	}

	public void setUsername(TextValue username, TextValue disc) {
		this.username = username;
		this.disc = disc;
		email = null;
		phone = null;
	}

	public void setPassword(HexHashValue passwordHash) {
		this.password = passwordHash;
	}

	public static LoginRequest withEmail(TextValue email, HexHashValue passwordHash) {
		LoginRequest loginRequest = new LoginRequest(passwordHash);
		loginRequest.setEmail(email);
		return loginRequest;
	}

	public static LoginRequest withPhone(TextValue phone, HexHashValue passwordHash) {
		LoginRequest loginRequest = new LoginRequest(passwordHash);
		loginRequest.setPhone(phone);
		return loginRequest;
	}

	public static LoginRequest withUsername(TextValue username, TextValue discriminant, HexHashValue passwordHash) {
		LoginRequest loginRequest = new LoginRequest(passwordHash);
		loginRequest.setUsername(username, discriminant);
		return loginRequest;
	}

	public TextValue getDisc() {
		return disc;
	}

	@Override
	protected void build(JSONObject object) {
		if (email != null)
			object.put(EMAIL_KEY, email.json());
		else if (phone != null)
			object.put(PHONE_KEY, phone.json());
		else {
			object.put(USERNAME_KEY, username.json());
			object.put(DISC_KEY, disc.json());
		}
		object.put(PASSWORD_KEY, password.json());
	}

	@Override
	public AuthTokenValue parseReturnValue(JSONValue json) {
		return new AuthTokenValue(json);
	}

	@Override
	public AuthTokenValue receiveResponse(CommunicationConnection client) throws LoginError, TFARequiredError,
			SyntaxError, RateLimitError, ServerError, RestrictedError, IllegalCommunicationProtocolException {
		try {
			return super.receiveResponse(client);
		} catch (LoginError | TFARequiredError | SyntaxError | RateLimitError | ServerError
				| RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}
