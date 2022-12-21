package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONValue;

public class BooleanValue implements CommunicationProtocolType {

	private final boolean val;

	public BooleanValue(boolean val) {
		this.val = val;
	}

	public BooleanValue(JSONValue json) {
		if (!(json instanceof JSONConstant))
			throw new CommunicationProtocolConstructionError("Expected Boolean, but found: " + json, json);
		switch (((JSONConstant) json)) {
		case FALSE:
			val = false;
			break;
		case TRUE:
			val = true;
			break;
		default:
			throw new CommunicationProtocolConstructionError("Expected Boolean, but found: " + json, json);
		}
	}

	public static final BooleanValue TRUE = new BooleanValue(true), FALSE = new BooleanValue(false);

	@Override
	public JSONValue json() {
		return val ? JSONConstant.TRUE : JSONConstant.FALSE;
	}

	public boolean is() {
		return val;
	}

	/**
	 * Returns a {@link BooleanValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * constructor for {@link BooleanValue}s.
	 * 
	 * @param value The {@link JSONValue} to construct the {@link BooleanValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link BooleanValue}, whichever represents the
	 *         provided argument.
	 */
	public static BooleanValue fromNullable(JSONValue value) {
		if (!(value instanceof JSONConstant))
			throw new CommunicationProtocolConstructionError("Expected Boolean or null, but found: " + value, value);
		switch ((JSONConstant) value) {
		case FALSE:
			return FALSE;
		case TRUE:
			return TRUE;
		case NULL:
			return null;
		default:
			throw new CommunicationProtocolConstructionError("Expected Boolean or null, but found: " + value, value);
		}
	}

}
