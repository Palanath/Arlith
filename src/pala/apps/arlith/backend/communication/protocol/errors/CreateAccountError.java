package pala.apps.arlith.backend.communication.protocol.errors;

import pala.apps.arlith.backend.communication.protocol.types.CreateAccountProblemValue;
import pala.libs.generic.json.JSONObject;

public class CreateAccountError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public CreateAccountProblemValue getType() {
		return type;
	}

	public void setType(CreateAccountProblemValue type) {
		this.type = type;
	}

	public CreateAccountError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = CreateAccountProblemValue.fromJSON(error.get(TYPE_KEY));
	}

	public CreateAccountError(CreateAccountProblemValue type, String message) {
		super(ERROR_TYPE, message);
		this.type = type;
	}

	public CreateAccountError(CreateAccountProblemValue type) {
		super(ERROR_TYPE);
		this.type = type;
	}

	public static final String ERROR_TYPE = "create-account";
	private static final String TYPE_KEY = "type";

	private CreateAccountProblemValue type;

	@Override
	protected void build(JSONObject object) {
		object.put(TYPE_KEY, type.json());
	}

}
