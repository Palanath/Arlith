package pala.apps.arlith.backend.common.protocol.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

abstract class CompoundType implements CommunicationProtocolType {

	private final Map<String, CommunicationProtocolType> values = new HashMap<>();

	protected final void put(String key, CommunicationProtocolType value) {
		values.put(key, value);
	}

	protected final void put(String key, boolean value) {
		put(key, new BooleanValue(value));
	}

	protected final void put(String key, int value) {
		put(key, new IntegerValue(value));
	}

	protected final void put(String key, long value) {
		put(key, new LongValue(value));
	}

	protected final void put(String key, String value) {
		put(key, new TextValue(value));
	}

	@SuppressWarnings("unchecked")
	protected final <T extends CommunicationProtocolType> T get(String key) {
		return (T) values.get(key);
	}

	public CompoundType(JSONValue json) {
		if (!(json instanceof JSONObject))
			throw new CommunicationProtocolConstructionError("Illegal value found: " + json, json);
		read((JSONObject) json);
	}

	public CompoundType() {
	}

	/**
	 * Expected to call {@link #extract(JSONObject, String, Function)} as many times
	 * as is needed for this compound value. This method is called from the
	 * constructor to construct a {@link CompoundType} given a
	 * {@link JSONObject}.
	 * 
	 * @param json The {@link JSONObject} to extract the attributes of.
	 * @author Palanath
	 */
	protected abstract void read(JSONObject json);

	protected final void extract(JSONObject map, String key, Function<JSONValue, ? extends CommunicationProtocolType> instantiator) {
		values.put(key, instantiator.apply(map.get(key)));
	}

	protected final void extract(JSONObject map, Function<JSONValue, ? extends CommunicationProtocolType> instantiator, String... keys) {
		for (String s : keys)
			extract(map, s, instantiator);
	}

	protected final void conditionalExtract(JSONObject map, String key,
			Function<JSONValue, ? extends CommunicationProtocolType> instantiator) {
		if (map.containsKey(key))
			extract(map, key, instantiator);
	}

	protected final void conditionalExtract(JSONObject map, Function<JSONValue, ? extends CommunicationProtocolType> instantiator,
			String... keys) {
		for (String s : keys)
			conditionalExtract(map, s, instantiator);
	}

	@Override
	public final JSONValue json() {
		JSONObject obj = new JSONObject();
		for (Entry<String, CommunicationProtocolType> e : values.entrySet())
			obj.put(e.getKey(), e.getValue().json());
		return obj;
	}

}
