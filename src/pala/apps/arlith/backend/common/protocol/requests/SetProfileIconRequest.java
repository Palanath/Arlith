package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * {@link CommunicationProtocolRequest} that sets the user's profile picture. This request sets
 * the currently logged in user's profile picture to be that specified, or
 * removes the profile picture if the specified {@link #profileIcon} is
 * <code>null</code>.
 * </p>
 * 
 * @author Palanath
 *
 */
public class SetProfileIconRequest extends CommunicationProtocolRequest<CompletionValue> {
	public static final String REQUEST_NAME = "set-profile-icon";

	/**
	 * The key for the profile icon data in the JSON package of this
	 * {@link CommunicationProtocolRequest}. The {@link #profileIcon} is a {@link PieceOMediaValue}
	 * object, and {@link PieceOMediaValue} objects store some JSON metadata about the
	 * media being sent over the network. In particular, (currently), the encode the
	 * size of the media as JSON. That size is stored in the JSON package of this
	 * request under this key.
	 */
	private static final String PROFILE_ICON_KEY = "media-length";

	/**
	 * The profile icon, or <code>null</code> if none is present.
	 */
	private PieceOMediaValue profileIcon;

	/**
	 * Creates a new {@link SetProfileIconRequest} with the provided media.
	 * 
	 * @param profileIcon The media to set the profile icon to, or <code>null</code>
	 *                    to remove the profile icon.
	 */
	public SetProfileIconRequest(PieceOMediaValue profileIcon) {
		super(REQUEST_NAME);
		this.profileIcon = profileIcon;
	}

	/**
	 * Reconstructs a {@link SetProfileIconRequest} from the provided JSON
	 * package and {@link Connection}.
	 * 
	 * @param properties The JSON package of the request.
	 * @param client     The {@link Connection} to read the auxiliary data from.
	 * @throws UnknownCommStateException In case reading auxiliary data from the
	 *                                   {@link Connection} results in an
	 *                                   {@link UnknownCommStateException}.
	 * @throws BlockException            In case reading auxiliary data from the
	 *                                   {@link Connection} results in a
	 *                                   {@link BlockException}.
	 */
	public SetProfileIconRequest(JSONObject properties, Connection client)
			throws UnknownCommStateException, BlockException {
		super(REQUEST_NAME, properties);
		profileIcon = properties.containsKey(PROFILE_ICON_KEY)
				? new PieceOMediaValue(properties.get(PROFILE_ICON_KEY), client)
				: null;
	}

	/**
	 * Gets the profile icon associated with this request, or <code>null</code> if
	 * this request is to remove the user's profile icon.
	 * 
	 * @return The piece of media representing the new profile icon, or
	 *         <code>null</code>.
	 */
	public PieceOMediaValue getProfileIcon() {
		return profileIcon;
	}

	/**
	 * Sets the profile icon associated with this request to be the specified piece
	 * of media. If the provided piece of media is <code>null</code>, then this
	 * object will then signify a request to remove the user's profile icon.
	 * 
	 * @param profileIcon The new profile icon, or <code>null</code>.
	 * @return This object for chaining convenience.
	 */
	public SetProfileIconRequest setProfileIcon(PieceOMediaValue profileIcon) {
		this.profileIcon = profileIcon;
		return this;
	}

	/**
	 * <p>
	 * Builds the JSON Package for this {@link SetProfileIconRequest}. The JSON
	 * package will be empty (an empty JSON Object) if {@link #profileIcon} is
	 * <code>null</code>. If {@link #profileIcon} is non-<code>null</code>, then the
	 * package will contain the the {@link #profileIcon}'s
	 * {@link PieceOMediaValue#json()} JSON data stored under the
	 * {@link #PROFILE_ICON_KEY}, {@value #PROFILE_ICON_KEY}.
	 * </p>
	 */
	@Override
	protected void build(JSONObject object) {
		if (profileIcon != null)
			object.put(PROFILE_ICON_KEY, profileIcon.json());
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	/**
	 * Sends the auxiliary data of this request over the specified connection, if
	 * any. If {@link #profileIcon} is <code>null</code>, this method does nothing.
	 * Otherwise, this method {@link PieceOMediaValue#send(Connection)}s the
	 * {@link #profileIcon} over the specified {@link Connection}.
	 */
	@Override
	protected void sendAuxiliaryData(CommunicationConnection connection) {
		if (profileIcon != null)
			profileIcon.sendAuxiliaryData(connection);
	}

	@Override
	protected CompletionValue parseReturnValue(JSONValue json, CommunicationConnection connection) {
		return new CompletionValue(json);
	}

}
