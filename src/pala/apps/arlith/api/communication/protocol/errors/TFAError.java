package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.types.TFAProblemValue;
import pala.libs.generic.json.JSONObject;

public class TFAError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public static final String TFA_ERROR_KEY = "type";
	public static final String ERROR_TYPE = "tfa";

	private final TFAProblemValue type;

	public TFAError(JSONObject error) {
		super(ERROR_TYPE, error);
		type = TFAProblemValue.fromJSON(error.get(TFA_ERROR_KEY));
	}

	public TFAError(TFAProblemValue error) {
		super(ERROR_TYPE);
		type = error;
	}

	public TFAProblemValue getLoginError() {
		return type;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(TFA_ERROR_KEY, type.json());
	}

}
