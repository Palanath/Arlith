package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError.CreateAccountProblemValue;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
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

	/**
	 * Returns an {@link EnumValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * constructor/factory function for {@link EnumValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link EnumValue} from, which
	 *              may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or an {@link EnumValue}, whichever represents the
	 *         provided argument.
	 */
	public static <E extends Enum<E>> EnumValue<E> fromNullable(JSONValue value, Class<E> enumType) {
		return value == JSONConstant.NULL ? null : new EnumValue<>(value, enumType);
	}

}
