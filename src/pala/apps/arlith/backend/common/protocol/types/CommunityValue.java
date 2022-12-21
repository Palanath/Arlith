package pala.apps.arlith.backend.common.protocol.types;

import pala.apps.arlith.backend.common.gids.GID;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class CommunityValue extends CompoundType {

	private static final String NAME_KEY = "name", THREADS_KEY = "threads", MEMBERS_KEY = "members", ID_KEY = "id";

	public CommunityValue(TextValue name, ListValue<ThreadValue> threads, ListValue<GIDValue> members, GIDValue id) {
		put(NAME_KEY, name);
		put(THREADS_KEY, threads);
		put(MEMBERS_KEY, members);
		put(ID_KEY, id);
	}

	public TextValue getName() {
		return get(NAME_KEY);
	}

	public String name() {
		return getName().getValue();
	}

	public ListValue<ThreadValue> getThreads() {
		return get(THREADS_KEY);
	}

	public ListValue<GIDValue> getMembers() {
		return get(MEMBERS_KEY);
	}

	public GIDValue getId() {
		return get(ID_KEY);
	}

	public GID id() {
		return getId().getGid();
	}

	public CommunityValue(JSONValue json) {
		super(json);
	}

	@Override
	protected void read(JSONObject json) {
		extract(json, NAME_KEY, TextValue::new);
		extract(json, THREADS_KEY, t -> new ListValue<>(t, ThreadValue::new));
		extract(json, MEMBERS_KEY, t -> new ListValue<>(t, GIDValue::new));
		extract(json, ID_KEY, GIDValue::new);
	}

	/**
	 * Returns a {@link CommunityValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link CommunityValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link CommunityValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link CommunityValue}, whichever represents the
	 *         provided argument.
	 */
	public static CommunityValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new CommunityValue(value);
	}

}
