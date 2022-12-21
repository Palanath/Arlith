package pala.apps.arlith.backend.communication.protocol.errors;

import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.libs.generic.json.JSONObject;

public class InvalidOptionError extends CommunicationProtocolError {

	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "invalid-option";
	private static final String OPTION_KEY = "option-key";

	private final TextValue option;

	public InvalidOptionError(JSONObject error) {
		super(ERROR_TYPE, error);
		option = new TextValue(error.get(OPTION_KEY));
	}

	public InvalidOptionError(TextValue option) {
		super(ERROR_TYPE);
		this.option = option;
	}

	public TextValue getOption() {
		return option;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(OPTION_KEY, option.json());
	}

}
