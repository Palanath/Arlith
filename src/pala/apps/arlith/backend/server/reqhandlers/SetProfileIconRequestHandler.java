package pala.apps.arlith.backend.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.requests.SetProfileIconRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Handles a request to set a user's profile icon
 * ({@link SetProfileIconRequest}).
 * </p>
 * <p>
 * This handler parses the request by constructing a
 * {@link SetProfileIconRequest} object, using
 * {@link SetProfileIconRequest#SetProfileIconRequest(JSONObject, pala.apps.arlith.backend.networking.networking.Connection)}.
 * </p>
 * <ol>
 * <li>The handler checks if any media is present (by seeing if
 * {@link SetProfileIconRequest#getProfileIcon()} is non-<code>null</code>).
 * <ul>
 * <li>If media is present (the {@link SetProfileIconRequest#getProfileIcon()
 * profile icon} in the request is non-<code>null</code>), then an attempt is
 * made to set {@link RequestConnection#getUser() the user's} profile icon,
 * using {@link ServerUser#setProfileIcon(byte[])}, to the
 * {@link PieceOMediaValue#getMedia() media received from the client}.
 * <ul>
 * <li>If this fails, then the stacktrace is printed and a new
 * {@link ServerError} is sent back, then handling completes.</li>
 * <li>If this succeeds, then the client is sent a new {@link CompletionValue},
 * and a new {@link LazyProfileIconChangedEvent} is fired to all the other users
 * that {@link ServerUser#getFriends() are friended with},
 * {@link ServerUser#getIncomingFriendRequestUsers() have an outgoing friend
 * request to}, or {@link ServerUser#getFriendRequestedUsers() have an incoming
 * friend request from}, this user.</li>
 * </ul>
 * </li>
 * <li>If media is not present (the
 * {@link SetProfileIconRequest#getProfileIcon() profile icon} in the request is
 * <code>null</code>), then the user's profile icon is removed (by calling
 * {@link ServerUser#setProfileIcon(byte[])} with <code>null</code> as the
 * argument). Then the client is sent a {@link CompletionValue}, and a
 * {@link LazyProfileIconChangedEvent} is fired to the other users that are
 * friended with this user or have an incoming friend request to, or outgoing
 * friend request from, this user.</li>
 * </ul>
 * </li>
 * </ol>
 * 
 * @author Palanath
 *
 */
public final class SetProfileIconRequestHandler implements RequestHandler {

	@Override
	public void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException {
		SetProfileIconRequest r = new SetProfileIconRequest(request, client.getConnection());

		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			PieceOMediaValue media = r.getProfileIcon();
			if (media == null)
				try {
					client.getUser().setProfileIcon(null);
				} catch (IOException | UnknownCommStateException | BlockException e) {
					e.printStackTrace();// Won't happen.
				}
			else {
				try {
					client.getUser().setProfileIcon(media.getMedia());
				} catch (IOException | UnknownCommStateException | BlockException e) {
					e.printStackTrace();
					client.sendError(new ServerError());
					return;
				}
			}
			client.sendResult(new CompletionValue());
			final LazyProfileIconChangedEvent event = new LazyProfileIconChangedEvent(new GIDValue(client.getUserID()));

			// TODO Synchronize
			client.getServer().getEventSystem().fire(event,
					JavaTools.mask(JavaTools.iterable(client.getUser().getFriends(),
							client.getUser().getIncomingFriendRequestUsers(),
							client.getUser().getFriendRequestedUsers()), ServerUser::getGID));
		}
	}
}
