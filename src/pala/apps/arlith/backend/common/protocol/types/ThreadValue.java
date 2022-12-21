package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.gids.GID;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ThreadValue extends CompoundType {
	private static final String NAME_KEY = "name", ID_KEY = "id";

	public TextValue getName() {
		return get(NAME_KEY);
	}

	public GIDValue getID() {
		return get(ID_KEY);
	}

	public String name() {
		return getName().getValue();
	}

	public GID id() {
		return getID().getGid();
	}

	@Override
	protected void read(JSONObject json) {
		extract(json, NAME_KEY, TextValue::new);
		extract(json, ID_KEY, GIDValue::new);
	}

	public ThreadValue(JSONValue json) {
		super(json);
	}

	public ThreadValue(TextValue name, GIDValue id) {
		put(NAME_KEY, name);
		put(ID_KEY, id);
	}

	/**
	 * Returns a {@link ThreadValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link ThreadValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link ThreadValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link ThreadValue}, whichever represents the
	 *         provided argument.
	 */
	public static ThreadValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new ThreadValue(value);
	}

}
