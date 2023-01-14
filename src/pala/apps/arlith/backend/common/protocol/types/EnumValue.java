package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONValue;

public class EnumValue<E extends Enum<E>> implements CommunicationProtocolType {

	private final E e;

	public EnumValue(E e) {
		this.e = e;
	}

	public EnumValue(JSONValue json, Class<E> enumType) {
		if (!(json instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError(
					"Provided JSON is not a number as it should be (for constructing an EnumValue from JSON). (Construction is done by enum value ordinal, which is why the JSON should be a JSONNumber.)",
					json);
		e = enumType.getEnumConstants()[((JSONNumber) json).intValue()];
	}

	@Override
	public JSONValue json() {
		return new JSONNumber(e.ordinal());
	}

}
