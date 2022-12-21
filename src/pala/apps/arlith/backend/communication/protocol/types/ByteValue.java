package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONValue;

public class ByteValue implements CommunicationProtocolType {

	private final byte value;

	public byte getValue() {
		return value;
	}

	public ByteValue(byte numb) {
		this.value = numb;
	}

	public ByteValue(JSONValue jsonValue) {
		if (!(jsonValue instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError("Expected Byte, but found: " + jsonValue, jsonValue);
		this.value = ((JSONNumber) jsonValue).byteValue();
	}

	@Override
	public JSONValue json() {
		return new JSONNumber(value);
	}

	/**
	 * Returns a {@link ByteValue} representing the provided argument if the provided
	 * argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link ByteValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link ByteValue} from, which may
	 *              represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link ByteValue}, whichever represents the
	 *         provided argument.
	 */
	public static ByteValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new ByteValue(value);
	}

}
