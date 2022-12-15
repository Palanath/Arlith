package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.types.LoginProblemValue;
import pala.libs.generic.json.JSONObject;

public class LoginError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	private static final String LOGIN_ERROR_KEY = "type";
	public static final String ERROR_TYPE = "login";

	private final LoginProblemValue type;

	public LoginError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = LoginProblemValue.fromJSON(error.get(LOGIN_ERROR_KEY));
	}

	public LoginError(LoginProblemValue error) {
		super(ERROR_TYPE);
		type = error;
	}

	public LoginProblemValue getLoginError() {
		return type;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(LOGIN_ERROR_KEY, type.json());
	}

}
