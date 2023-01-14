package pala.apps.arlith.backend.common.protocol.errors;

import pala.apps.arlith.backend.common.protocol.types.EnumValue;
import pala.libs.generic.json.JSONObject;

public class CreateAccountError extends CommunicationProtocolError {

	public enum CreateAccountProblem {
		ILLEGAL_UN, SHORT_UN, LONG_UN, TAKEN_UN, ILLEGAL_EM, LONG_EM, TAKEN_EM, ILLEGAL_PW, SHORT_PW, LONG_PW,
		ILLEGAL_PH, SHORT_PH, LONG_PH, TAKEN_PH;
	}

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public EnumValue<CreateAccountProblem> getType() {
		return type;
	}

	public void setType(EnumValue<CreateAccountProblem> type) {
		this.type = type;
	}

	public CreateAccountError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = new EnumValue<>(error.get(TYPE_KEY), CreateAccountProblem.class);
	}

	public CreateAccountError(EnumValue<CreateAccountProblem> type, String message) {
		super(ERROR_TYPE, message);
		this.type = type;
	}

	public CreateAccountError(EnumValue<CreateAccountProblem> type) {
		super(ERROR_TYPE);
		this.type = type;
	}

	public CreateAccountError(CreateAccountProblem type, String message) {
		this(new EnumValue<>(type), message);
	}

	public CreateAccountError(CreateAccountProblem type) {
		this(new EnumValue<>(type));
	}

	public static final String ERROR_TYPE = "create-account";
	private static final String TYPE_KEY = "type";

	private EnumValue<CreateAccountProblem> type;

	@Override
	protected void build(JSONObject object) {
		object.put(TYPE_KEY, type.json());
	}

}
