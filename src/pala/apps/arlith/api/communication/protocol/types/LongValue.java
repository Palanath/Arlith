package pala.apps.arlith.api.communication.protocol.types;

import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONValue;

public class LongValue implements CommunicationProtocolType {

	private final long value;

	public long getValue() {
		return value;
	}

	public LongValue(long value) {
		this.value = value;
	}

	public LongValue(JSONValue jsonValue) {
		if (!(jsonValue instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError("Expected Long, but found: " + jsonValue, jsonValue);
		this.value = ((JSONNumber) jsonValue).longValue();
	}

	@Override
	public JSONValue json() {
		return new JSONNumber(value);
	}

	/**
	 * Returns a {@link LongValue} representing the provided argument if the provided
	 * argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link LongValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link LongValue} from, which may
	 *              represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link LongValue}, whichever represents the
	 *         provided argument.
	 */
	public static LongValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new LongValue(value);
	}

}
