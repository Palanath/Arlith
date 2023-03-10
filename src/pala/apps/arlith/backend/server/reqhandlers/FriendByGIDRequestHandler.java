package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.requests.FriendByGIDRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class FriendByGIDRequestHandler extends SimpleRequestHandler<FriendByGIDRequest> {

	@Override
	protected void handle(final FriendByGIDRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		// Check if client is authorized.
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		// Get the user that the friend request refers to.
		ServerUser target;
		if ((target = client.getWorld().getUserByID(r.getId().getGid())) == null) {
			client.sendError(new ObjectNotFoundError(r.getId()));
			return;
		}
		if (target.getGID().equals(client.getUserID())) {
			client.sendError(new ObjectNotFoundError());
			return;
		}
		ServerUser cl = client.getUser();

		// TODO Update to return whether something happened. In that case, we'll send
		// over an event.
		ServerUser.FriendState prev = target.getFriendState(cl);
		cl.friend(target);
		ServerUser.FriendState resulting = target.getFriendState(cl);

		client.sendResult(new CompletionValue());
		if (prev != resulting)
			client.getServer().getEventSystem().fire(new IncomingFriendEvent(new GIDValue(cl.getGID()), prev.toCommunicationProtocolState(),
					resulting.toCommunicationProtocolState(), new GIDValue(new GID())), target);
	}

	public FriendByGIDRequestHandler() {
		super(FriendByGIDRequest::new);
	}
}
