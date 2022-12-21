package pala.apps.arlith.backend.common.protocol.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONArray;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONValue;

public class ListValue<I extends CommunicationProtocolType> extends BoundedTypeValue implements CommunicationProtocolType, Iterable<I> {

	private final List<I> array;

	public ListValue(JSONValue json, Function<JSONValue, I> extractor) {
		if (!(json instanceof JSONArray))
			throw new CommunicationProtocolConstructionError("Expected a List, but found: " + json, json);
		array = new ArrayList<>();
		for (JSONValue j : (JSONArray) json)
			array.add(extractor.apply(j));
	}

	/**
	 * Returns a {@link ListValue} representing the provided argument if the provided
	 * argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link ListValue}s.
	 * 
	 * @param value     The {@link JSONValue} to get the {@link ListValue} from, which
	 *                  may represent <code>null</code> (by being
	 *                  {@link JSONConstant#NULL}).
	 * @param extractor The object used to populate this {@link ListValue} with
	 *                  {@link CommunicationProtocolType} values by reading the entries of the
	 *                  {@link JSONArray}.
	 * @return <code>null</code> or a {@link ListValue}, whichever represents the
	 *         provided argument.
	 * @param <I> The type of the values stored in the returned {@link ListValue}
	 * 
	 */
	public static <I extends CommunicationProtocolType> ListValue<I> fromNullable(JSONValue value, Function<JSONValue, I> extractor) {
		return value == JSONConstant.NULL ? null : new ListValue<I>(value, extractor);
	}

	public ListValue(List<? extends I> items) {
		array = new ArrayList<>(items);
	}

	public ListValue(Iterator<? extends I> items) {
		array = new ArrayList<>();
		while (items.hasNext())
			array.add(items.next());
	}

	public ListValue(List<? extends I> items, Integer upperBound, Integer lowerBound) {
		super(lowerBound, upperBound);
		array = new ArrayList<>(items);
	}

	@SafeVarargs
	private static <I> List<I> list(I... items) {
		List<I> l = new ArrayList<>();
		for (I i : items)
			l.add(i);
		return l;
	}

	@SafeVarargs
	public ListValue(I... items) {
		this(list(items));
	}

	public Iterable<JSONValue> jsonIter() {
		return JavaTools.mask(this, I::json);
	}

	public I get(int index) {
		return array.get(index);
	}

	@Override
	public JSONValue json() {
		return new JSONArray(jsonIter());
	}

	@Override
	public Integer size() {
		return array.size();
	}

	public List<I> getBackingUnmodifiable() {
		return Collections.unmodifiableList(array);
	}

	/**
	 * Returns {@link #getBackingUnmodifiable()}<code>.</code>{@link List#iterator()
	 * iterator()}
	 */
	@Override
	public Iterator<I> iterator() {
		return getBackingUnmodifiable().iterator();
	}

}
