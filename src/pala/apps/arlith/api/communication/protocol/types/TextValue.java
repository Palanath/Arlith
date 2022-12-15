package pala.apps.arlith.api.communication.protocol.types;

import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public final class TextValue extends BoundedTypeValue implements CommunicationProtocolType {

	public TextValue(String value) {
		this.value = value;
	}

	public TextValue(JSONValue json) throws CommunicationProtocolConstructionError {// No bound constraints
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Expected Text, but found: " + json, json);
		this.value = ((JSONString) json).getValue();
	}

	public TextValue() {
		value = "";
	}

	public TextValue(int minimum, String value) {
		super(minimum);
		this.value = value;
		checkSize();
	}

	public TextValue(Integer bound, boolean lower, String value) {
		super(bound, lower);
		this.value = value;
		checkSize();
	}

	public TextValue(Integer min, Integer max, String value) {
		super(min, max);
		this.value = value;
		checkSize();
	}

	public static TextValue lowerBounded(String text, int min) {
		return new TextValue(min, text);
	}

	public static TextValue upperBounded(String text, int max) {
		return new TextValue(max, false, text);
	}

	public String getValue() {
		return value;
	}

	private final String value;

	@Override
	public final JSONValue json() {
		return new JSONString(value == null ? "" : value);
	}

	@Override
	public Integer size() {
		return value == null ? null : value.length();
	}

	/**
	 * Returns a {@link TextValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link TextValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link TextValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link TextValue}, whichever represents the
	 *         provided argument.
	 */
	public static TextValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new TextValue(value);
	}

}
