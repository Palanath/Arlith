package pala.apps.arlith.api.communication.protocol.types;

import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONValue;

public class CompletionValue implements CommunicationProtocolType {

	public CompletionValue(JSONValue json) {
		if (json != JSONConstant.TRUE)
			throw new IllegalArgumentException("Expected the JSON constant \"true\" but found: " + json);
	}

	public CompletionValue() {
	}

	@Override
	public JSONValue json() {
		return JSONConstant.TRUE;
	}

	/**
	 * Returns a {@link CompletionValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link CompletionValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link CompletionValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link CompletionValue}, whichever represents
	 *         the provided argument.
	 */
	public static CompletionValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new CompletionValue(value);
	}
}
