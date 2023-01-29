package pala.apps.arlith.backend.common.protocol.errors;

import pala.apps.arlith.backend.common.protocol.types.EnumValue;
import pala.libs.generic.json.JSONObject;

public class ChangeEmailError extends CommunicationProtocolError {

	public static final String ERROR_TYPE = "change-email";
	private static final String TYPE_KEY = "type";

	private EnumValue<ErrorType> type;

	public ChangeEmailError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = new EnumValue<>(error.get(TYPE_KEY), ErrorType.class);
	}

	public ChangeEmailError(ErrorType type) {
		this(new EnumValue<>(type));
	}

	public ChangeEmailError(ErrorType type, String message) {
		this(new EnumValue<>(type), message);
	}

	public ChangeEmailError(EnumValue<ErrorType> type) {
		super(ERROR_TYPE);
		this.type = type;
	}

	public ChangeEmailError(EnumValue<ErrorType> type, String message) {
		super(ERROR_TYPE, message);
		this.type = type;
	}

	public enum ErrorType {
		/**
		 * Indicates that the email address is already associated with another account.
		 */
		EMAIL_ALREADY_IN_USE,
		/**
		 * Indicates that the provided string is not a valid email address.
		 */
		EMAIL_SYNTACTICALLY_INVALID
	}

	public EnumValue<ErrorType> getType() {
		return type;
	}
	
	public ErrorType getTypeValue() {
		return getType().get();
	}
	
	@Override
	protected void build(JSONObject object) {
		object.put(TYPE_KEY, type.json());
	}

}
