package pala.apps.arlith.backend.communication.protocol.types;

import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.requests.GetUserRequest;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * Represents a single user under the Communication Protocol. This class is used
 * by both the client and server to send and receive information about a user.
 * </p>
 * <p>
 * This type is a <b>response</b> type, meaning that it's chiefly sent by the
 * server, to the client, in response to certain requests. For example, when the
 * client has a user's {@link GID} and wants to request additional information
 * about that user, (e.g. to render that user's profile, name, status, etc.),
 * the client would send a {@link GetUserRequest} to the server. The server
 * would respond with a {@link UserValue} object that contains the requested
 * user's information.
 * </p>
 * <p>
 * {@link UserValue} objects contain all of the simple metadata about users:
 * </p>
 * <style> table, th, td { border: solid 1px currentcolor; border-collapse:
 * collapse; } th, td { padding: 0.3em; } </style>
 * <table>
 * <tr>
 * <th>Datum</th>
 * <th>Type</th>
 * <th>Description</th>
 * <th>Examples</th>
 * </tr>
 * <tr>
 * <td>Username</td>
 * <td>{@link TextValue}</td>
 * <td>The username of the user. This does not include the <code>#</code> or
 * discriminator.</td>
 * <td><code>Tobuscus</code><br>
 * <code>ABC</code><br>
 * <code>1_2_3--Joe</code></td>
 * </tr>
 * <tr>
 * <td>Status</td>
 * <td>{@link TextValue}</td>
 * <td>The current status of the user. This is an arbitrary, limited-size string
 * that gets set by the user. It is rendered by the client inside this user's
 * profile.</td>
 * <td>Playing a game.<br>
 * XYZ<br>
 * Out shopping.</td>
 * </tr>
 * <tr>
 * <td>Message Count</td>
 * <td>{@link LongValue}</td>
 * <td>The total number of messages that this user has sent on the platform at
 * the time this {@link UserValue} object was created.</td>
 * <td>0<br>
 * 1<br>
 * 1000<br>
 * 1258198</td>
 * </tr>
 * </table>
 * 
 * @author Palanath
 *
 */
public class UserValue extends CompoundType {
	private static final String USERNAME_KEY = "username", STATUS_KEY = "status", MESSAGE_COUNT_KEY = "message-count",
			ID_KEY = "id", DISC_KEY = "disc";

	public UserValue(JSONValue json) {
		super(json);
	}

	public TextValue getUsername() {
		return get(USERNAME_KEY);
	}

	public TextValue getStatus() {
		return get(STATUS_KEY);
	}

	public LongValue getMessageCount() {
		return get(MESSAGE_COUNT_KEY);
	}

	public TextValue getDiscriminant() {
		return get(DISC_KEY);
	}

	public String username() {
		return getUsername().getValue();
	}

	public String status() {
		return getStatus().getValue();
	}

	public long messageCount() {
		return getMessageCount().getValue();
	}

	public String discriminant() {
		return getDiscriminant().getValue();
	}

	public GIDValue getId() {
		return get(ID_KEY);
	}

	public GID id() {
		return getId().getGid();
	}

	public UserValue(TextValue username, TextValue status, LongValue messageCount, TextValue disc, GIDValue id) {
		put(USERNAME_KEY, username);
		put(STATUS_KEY, status);
		put(MESSAGE_COUNT_KEY, messageCount);
		put(DISC_KEY, disc);
		put(ID_KEY, id);
	}

	@Override
	protected void read(JSONObject json) {
		extract(json, USERNAME_KEY, TextValue::new);
		extract(json, STATUS_KEY, TextValue::new);
		extract(json, MESSAGE_COUNT_KEY, LongValue::new);
		extract(json, DISC_KEY, TextValue::new);
		extract(json, ID_KEY, GIDValue::new);
	}

	/**
	 * Returns a {@link UserValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link UserValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link UserValue} from, which
	 *              may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link UserValue}, whichever represents the
	 *         provided argument.
	 */
	public static UserValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new UserValue(value);
	}

}
