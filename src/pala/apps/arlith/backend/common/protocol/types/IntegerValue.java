package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONValue;

public class IntegerValue implements CommunicationProtocolType {

	private final int value;

	public int getValue() {
		return value;
	}

	public IntegerValue(int numb) {
		this.value = numb;
	}

	public IntegerValue(JSONValue jsonValue) {
		if (!(jsonValue instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError("Expected Integer, but found: " + jsonValue, jsonValue);
		this.value = ((JSONNumber) jsonValue).intValue();
	}

	@Override
	public JSONValue json() {
		return new JSONNumber(value);
	}

	/**
	 * Returns a {@link IntegerValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link IntegerValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link IntegerValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link IntegerValue}, whichever represents the
	 *         provided argument.
	 */
	public static IntegerValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new IntegerValue(value);
	}

}
