package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public enum AuthProblemValue implements CommunicationProtocolType {
	INVALID_TOKEN;

	public static AuthProblemValue fromJSON(JSONValue json) throws CommunicationProtocolConstructionError {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Value is not of the correct JSON type for an AuthProblem.", json);
		return fromJSONString((JSONString) json);
	}

	public static AuthProblemValue fromJSONString(JSONString json) {
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
	 * Returns a {@link AuthProblemValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link AuthProblemValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link AuthProblemValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link AuthProblemValue}, whichever represents
	 *         the provided argument.
	 */
	public static AuthProblemValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : fromJSON(value);
	}

}
