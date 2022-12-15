package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONObject;

public class SyntaxError extends CommunicationProtocolError {

	public static final String ERROR_TYPE = "syntax";

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public SyntaxError(JSONObject error) {
		super(ERROR_TYPE, error);
	}

	public SyntaxError() {
		super(ERROR_TYPE);
	}

	public SyntaxError(String msg) {
		super(ERROR_TYPE, msg);
	}

	public SyntaxError(CommunicationProtocolConstructionError base) {
		super(ERROR_TYPE, base.getMessage());
		initCause(base);
	}

	@Override
	protected void build(JSONObject object) {
	}

}
