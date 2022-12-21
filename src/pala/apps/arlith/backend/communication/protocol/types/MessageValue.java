package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.gids.GID;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class MessageValue extends CompoundType {
	private static final String CONTENT_KEY = "content", SENDER_KEY = "sender", THREAD_KEY = "thread", ID_KEY = "id";

	public MessageValue(TextValue content, GIDValue sender, GIDValue ownerThread, GIDValue messageID) {
		put(CONTENT_KEY, content);
		put(SENDER_KEY, sender);
		put(THREAD_KEY, ownerThread);
		put(ID_KEY, messageID);
	}

	public MessageValue(JSONValue json) {
		super(json);
	}

	public TextValue getContent() {
		return get(CONTENT_KEY);
	}

	public GIDValue getSenderUser() {
		return get(SENDER_KEY);
	}

	public GIDValue getOwnerThread() {
		return get(THREAD_KEY);
	}

	public GIDValue getId() {
		return get(ID_KEY);
	}

	public String content() {
		return getContent().getValue();
	}

	public GID senderUser() {
		return getSenderUser().getGid();
	}

	public GID ownerThread() {
		return getOwnerThread().getGid();
	}

	public GID id() {
		return getId().getGid();
	}

	@Override
	protected void read(JSONObject json) {
		extract(json, CONTENT_KEY, TextValue::new);
		extract(json, GIDValue::new, SENDER_KEY, THREAD_KEY, ID_KEY);
	}

	/**
	 * Returns a {@link MessageValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link MessageValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link MessageValue} from, which
	 *              may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link MessageValue}, whichever represents the
	 *         provided argument.
	 */
	public static MessageValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new MessageValue(value);
	}

}
