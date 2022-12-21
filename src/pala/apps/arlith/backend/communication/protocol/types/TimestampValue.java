package pala.apps.arlith.backend.communication.protocol.types;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONValue;

public class TimestampValue implements CommunicationProtocolType {

	public Instant getValue() {
		return value;
	}

	private final Instant value;

	public TimestampValue(Instant value) {
		JavaTools.requireNonNull(value);
		this.value = value;
	}

	public TimestampValue(JSONValue json) {
		if (!(json instanceof JSONArray))
			throw new CommunicationProtocolConstructionError("Expected Timestamp, but found: " + json, json);
		JSONArray jar = (JSONArray) json;
		if (jar.size() != 2)
			throw new CommunicationProtocolConstructionError("Expected Timestamp, but found: " + json, json);
		if (!(jar.get(0) instanceof JSONNumber && jar.get(1) instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError("Expected Timestamp, but found: " + json, json);
		value = Instant.ofEpochSecond(((JSONNumber) jar.get(0)).longValue(), ((JSONNumber) jar.get(1)).intValue());
	}

	@Override
	public JSONValue json() {
		return new JSONArray(new JSONNumber(value.getEpochSecond()), new JSONNumber(value.getNano()));
	}

	/**
	 * Returns a {@link TimestampValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link TimestampValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link TimestampValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link TimestampValue}, whichever represents the
	 *         provided argument.
	 */
	public static TimestampValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new TimestampValue(value);
	}
}
