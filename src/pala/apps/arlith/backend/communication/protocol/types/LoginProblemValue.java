package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public enum LoginProblemValue implements CommunicationProtocolType {
	ILLEGAL_UN, SHORT_UN, LONG_UN, INVALID_UN, ILLEGAL_PW, SHORT_PW, LONG_PW, INVALID_PW, ILLEGAL_EM, SHORT_EM, LONG_EM,
	INVALID_EM, ILLEGAL_PH, SHORT_PH, LONG_PH, INVALID_PH;

	public static LoginProblemValue fromJSON(JSONValue json) {
		return fromJSONString((JSONString) json);
	}

	public static LoginProblemValue fromJSONString(JSONString json) {
		try {
			return valueOf(json.getValue());
		} catch (IllegalArgumentException e) {
			throw new CommunicationProtocolConstructionError(e, json);
		}
	}

	@Override
	public JSONValue json() {
		return new JSONString(name());
	}

	/**
	 * Returns a {@link LoginProblemValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link LoginProblemValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link LoginProblemValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link LoginProblemValue}, whichever represents
	 *         the provided argument.
	 */
	public static LoginProblemValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : fromJSON(value);
	}

}
