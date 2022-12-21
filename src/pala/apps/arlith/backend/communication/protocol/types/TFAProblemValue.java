package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public enum TFAProblemValue implements CommunicationProtocolType {
	ILLEGAL_CODE, INVALID_CODE;

	public static TFAProblemValue fromJSON(JSONValue json) throws CommunicationProtocolConstructionError {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Value is not of the correct JSON type for a TFAProblem.", json);
		return fromJSONString((JSONString) json);
	}

	public static TFAProblemValue fromJSONString(JSONString json) {
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
	 * Returns a {@link TFAProblemValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link TFAProblemValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link TFAProblemValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link TFAProblemValue}, whichever represents
	 *         the provided argument.
	 */
	public static TFAProblemValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : fromJSON(value);
	}
}
