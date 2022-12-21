package pala.apps.arlith.frontend.server.reqhandlers;

import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.communication.protocol.requests.FriendByGIDRequest;
import pala.apps.arlith.backend.communication.protocol.types.CompletionValue;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;

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
