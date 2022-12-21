package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public enum CreateAccountProblemValue implements CommunicationProtocolType {
	ILLEGAL_UN, SHORT_UN, LONG_UN, TAKEN_UN, ILLEGAL_EM, LONG_EM, TAKEN_EM, ILLEGAL_PW, SHORT_PW, LONG_PW, ILLEGAL_PH,
	SHORT_PH, LONG_PH, TAKEN_PH;

	public static CreateAccountProblemValue fromJSON(JSONValue json) {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError(
					"Value is not of the correct JSON type for a CreateAccountProblem.", json);
		return fromJSONString((JSONString) json);
	}

	public static CreateAccountProblemValue fromJSONString(JSONString string) {
		try {
			return valueOf(string.getValue());
		} catch (IllegalArgumentException e) {
			throw new CommunicationProtocolConstructionError(e, string);
		}
	}

	@Override
	public JSONValue json() {
		return new JSONString(name());
	}

	/**
	 * Returns a {@link CreateAccountProblemValue} representing the provided
	 * argument if the provided argument is not {@link JSONConstant#NULL},
	 * otherwise, returns <code>null</code>. This is essentially the
	 * "<code>null</code>-safe" <code>from</code> method for
	 * {@link CreateAccountProblemValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the
	 *              {@link CreateAccountProblemValue} from, which may represent
	 *              <code>null</code> (by being {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link CreateAccountProblemValue}, whichever
	 *         represents the provided argument.
	 */
	public static CreateAccountProblemValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : fromJSON(value);
	}

}
