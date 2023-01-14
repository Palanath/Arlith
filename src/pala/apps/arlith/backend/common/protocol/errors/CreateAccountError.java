package pala.apps.arlith.backend.common.protocol.errors;

import pala.apps.arlith.backend.common.protocol.types.EnumValue;
import pala.libs.generic.json.JSONObject;

public class CreateAccountError extends CommunicationProtocolError {

	public enum CreateAccountProblem {
		/**
		 * Indicates that the username is not syntactically valid.
		 */
		USERNAME_SYNTACTICALLY_INVALID,
		/**
		 * Indicates that the email address is not syntactically valid.
		 */
		EMAIL_SYNTACTICALLY_INVALID,
		/**
		 * Indicates that the email address is already associated with another account.
		 */
		EMAIL_ALREADY_IN_USE,
		/**
		 * Indicates that the password is not syntactically valid.
		 */
		PASSWORD_SYNTACTICALLY_INVALID,
		/**
		 * Indicates that the phone number is not syntactically valid.
		 */
		PHONE_NUMBER_SYNTACTICALLY_INVALID,
		/**
		 * Indicates that the phone number is already associated with another account.
		 */
		PHONE_NUMBER_ALREADY_IN_USE;
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
