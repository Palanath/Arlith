package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public enum FriendStateValue implements CommunicationProtocolType {
	/**
	 * <p>
	 * Indicates that the other user in question has sent a friend request to this
	 * user, and not vice versa. Specifically, this is the only social link of a
	 * friend nature between the user represented by the utilizing client and the
	 * other user in question.
	 * </p>
	 * <p>
	 * Representing the user of the utilizing client and the other user in question
	 * as "T" and "O," respectively, we can display this relationship as follows:
	 * 
	 * <pre>
	 * 	<code>T &lt;-- O</code>
	 * </pre>
	 * 
	 * where <code>&lt;</code> represents the notion that an active friend request
	 * exists from <code>O</code> to <code>T</code>.
	 * </p>
	 */
	INCOMING,
	/**
	 * <p>
	 * Indicates that the user associated with the utilizing client has sent a
	 * friend request to the other user in question, and not vice versa.
	 * Specifically, this is the only social link of a friend nature between the
	 * user represented by the utilizing client and the other user in question.
	 * </p>
	 * <p>
	 * Representing the user of the utilizing client and the other user in question
	 * as "T" and "O," respectively, we can display this relationship as follows:
	 * 
	 * <pre>
	 * 	<code>T --&gt; O</code>
	 * </pre>
	 * 
	 * where <code>&gt;</code> represents the notion that an active friend request
	 * exists from <code>T</code> to <code>O</code>.
	 * </p>
	 */
	OUTGOING,
	/**
	 * <p>
	 * Indicates that the user associated with the utilizing client and the other
	 * user in question both created active friend requests that persisted at the
	 * same moment. (This indicates that the users are friended with each other.)
	 * </p>
	 * <p>
	 * Representing the user of the utilizing client and the other user in question
	 * as "T" and "O," respectively, we can display this relationship as follows:
	 * 
	 * <pre>
	 * 	<code>T &lt;-&gt; O</code>
	 * </pre>
	 * 
	 * where <code>&lt;</code> represents the notion that an active friend request
	 * exists from <code>O</code> to <code>T</code> and <code>&gt;</code> represents
	 * the notion that an active friend request exists from <code>T</code> to
	 * <code>O</code>.
	 * </p>
	 */
	FRIENDED,
	/**
	 * <p>
	 * Indicates that neither the user associated with the utilizing client nor the
	 * other user in question have any social link of a friend nature. (This means
	 * that no active friend request exists between the two users, nor does a
	 * friendship.)
	 * </p>
	 * <p>
	 * 
	 * <pre>
	 * <code>T --- O</code>
	 * </pre>
	 * 
	 * where <code>&lt;</code> represents the notion that an active friend request
	 * exists from <code>O</code> to <code>T</code> and <code>&gt;</code> represents
	 * the notion that an active friend request exists from <code>T</code> to
	 * <code>O</code>.
	 * </p>
	 */
	NONE;

	public static FriendStateValue fromJSON(JSONValue json) throws CommunicationProtocolConstructionError {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError(
					"Value is not of the correct JSON type for a Friend State.", json);
		return fromJSONString((JSONString) json);
	}

	public static FriendStateValue fromJSONString(JSONString json) {
		try {
			return valueOf(json.getValue());
		} catch (IllegalArgumentException e) {
			throw new CommunicationProtocolConstructionError(e, json);
		}
	}

	@Override
	public JSONValue json() {
		return new JSONString(name());
	}

	/**
	 * Returns a {@link FriendStateValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link FriendStateValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link FriendStateValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link FriendStateValue}, whichever represents
	 *         the provided argument.
	 */
	public static FriendStateValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : fromJSON(value);
	}

}
