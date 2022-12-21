package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.authentication.AuthToken;
import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public class AuthTokenValue implements CommunicationProtocolType {

	private final AuthToken token;

	public AuthTokenValue(AuthToken token) {
		this.token = token;
	}

	public AuthTokenValue(String token) {
		this(AuthToken.fromString(token));
	}

	public AuthTokenValue(JSONValue json) throws CommunicationProtocolConstructionError {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Expected AuthToken, but found: " + json, json);
		try {
			token = AuthToken.fromString(((JSONString) json).getValue());
		} catch (NumberFormatException e) {
			throw new CommunicationProtocolConstructionError(e, json);
		}
	}

	public AuthToken getToken() {
		return token;
	}

	@Override
	public JSONValue json() {
		return new JSONString(token.toString());
	}

	/**
	 * Returns a {@link AuthTokenValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link AuthTokenValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link AuthTokenValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link AuthTokenValue}, whichever represents the
	 *         provided argument.
	 */
	public static AuthTokenValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new AuthTokenValue(value);
	}

}
