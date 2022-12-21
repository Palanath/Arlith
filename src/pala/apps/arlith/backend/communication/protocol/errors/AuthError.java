package pala.apps.arlith.backend.communication.protocol.errors;

import pala.apps.arlith.backend.communication.protocol.types.AuthProblemValue;
import pala.libs.generic.json.JSONObject;

public class AuthError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public static final String AUTH_ERROR_KEY = "type";
	public static final String ERROR_TYPE = "auth";

	private final AuthProblemValue type;

	public AuthError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = AuthProblemValue.fromJSON(error.get(AUTH_ERROR_KEY));
	}

	public AuthError(AuthProblemValue error) {
		super(ERROR_TYPE);
		type = error;
	}

	public AuthProblemValue getLoginError() {
		return type;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(AUTH_ERROR_KEY, type.json());
	}

}
