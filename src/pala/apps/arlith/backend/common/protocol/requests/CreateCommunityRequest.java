package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * Represents a request to the server to create a new Community.
 * </p>
 * 
 * <style> table, th, td { border: solid 1px currentcolor; border-collapse:
 * collapse; } th, td { padding: 0.3em; } </style>
 * <table>
 * <tr>
 * <th>Parameter Name</th>
 * <th>Type</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>Name</td>
 * <td>{@link TextValue}</td>
 * <td>The name of the new community.</td>
 * </tr>
 * <tr>
 * <td>Icon</td>
 * <td>{@link PieceOMediaValue}</td>
 * <td>The community's icon. <b>Optional</b> - Can be <code>null</code>.</td>
 * </tr>
 * <tr>
 * <td>Background</td>
 * <td>{@link PieceOMediaValue}</td>
 * <td>The community's background image. <b>Optional</b> - Can be
 * <code>null</code>.</td>
 * </tr>
 * </table>
 * 
 * <ul>
 * <li>This request responds with a {@link CompletionValue}.</li>
 * </ul>
 * 
 * 
 * @author Palanath
 *
 */
public class CreateCommunityRequest extends CommunicationProtocolRequest<CommunityValue> {
	public static final String REQUEST_NAME = "create-community";

	private static final String NAME_KEY = "name", ICON_KEY = "icon", BACKGROUND_KEY = "background";

	private TextValue name;
	private PieceOMediaValue icon, background;

	public CreateCommunityRequest(TextValue name, PieceOMediaValue icon, PieceOMediaValue background) {
		super(REQUEST_NAME);
		this.name = name;
		this.icon = icon;
		this.background = background;
	}

	public CreateCommunityRequest(JSONObject properties, CommunicationConnection connection) {
		super(REQUEST_NAME, properties);
		name = new TextValue(properties.get(NAME_KEY));
		if (properties.containsKey(ICON_KEY))
			icon = new PieceOMediaValue(properties.get(ICON_KEY), connection);
		if (properties.containsKey(BACKGROUND_KEY))
			background = new PieceOMediaValue(properties.get(BACKGROUND_KEY), connection);
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this request contains an icon for the community,
	 * and <code>false</code> otherwise.
	 * </p>
	 * <p>
	 * This method returns:
	 * </p>
	 * 
	 * <pre>{@link #getIcon()}!=<code>null</code></pre>
	 * 
	 * @return Whether there is an icon being sent with this request.
	 */
	public boolean hasIcon() {
		return getIcon() != null;
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this request contains a background for the
	 * community, and <code>false</code> otherwise.
	 * </p>
	 * <p>
	 * This method returns:
	 * </p>
	 * 
	 * <pre>{@link #getBackground()}!=<code>null</code></pre>
	 * 
	 * @return Whether there is a background being sent with this request.
	 */
	public boolean hasBackground() {
		return getBackground() != null;
	}

	public TextValue getName() {
		return name;
	}

	public CreateCommunityRequest setName(TextValue name) {
		this.name = name;
		return this;
	}

	public PieceOMediaValue getIcon() {
		return icon;
	}

	public CreateCommunityRequest setIcon(PieceOMediaValue icon) {
		this.icon = icon;
		return this;
	}

	public PieceOMediaValue getBackground() {
		return background;
	}

	public CreateCommunityRequest setBackground(PieceOMediaValue background) {
		this.background = background;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(NAME_KEY, name.json());
		if (icon != null)
			object.put(ICON_KEY, icon.json());
		if (background != null)
			object.put(BACKGROUND_KEY, background.json());
	}

	@Override
	protected void sendAuxiliaryData(CommunicationConnection connection) {
		if (icon != null)
			icon.sendAuxiliaryData(connection);
		if (background != null)
			background.sendAuxiliaryData(connection);
	}

	@Override
	public CommunityValue receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected CommunityValue parseReturnValue(JSONValue json, CommunicationConnection connection) {
		return new CommunityValue(json);
	}

}