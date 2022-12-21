package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.requests.FriendByNameRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class FriendByNameRequestHandler extends SimpleRequestHandler<FriendByNameRequest> {

	@Override
	protected void handle(final FriendByNameRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		// Check if client is authorized.
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		// Get the user that the friend request refers to.
		ServerUser target;
		// TODO Change type of discriminator inside FriendByNameRequest to int.
		if ((target = client.getWorld().getUserByUsername(r.getUsername().getValue(), r.getDisc().getValue())) == null
				|| target.getGID().equals(client.getUserID())) {
			client.sendError(new ObjectNotFoundError());
			return;
		}

		ServerUser cl = client.getUser();
		// TODO Make friend(...) return a boolean determining whether something
		// happened, and then use that to determine when to fire an event.
		ServerUser.FriendState prev = target.getFriendState(cl);
		cl.friend(target);
		ServerUser.FriendState resulting = target.getFriendState(cl);
		client.sendResult(new GIDValue(target.getGID()));
		if (prev != resulting)
			client.getServer().getEventSystem().fire(new IncomingFriendEvent(new GIDValue(cl.getGID()), prev.toCommunicationProtocolState(),
					resulting.toCommunicationProtocolState(), new GIDValue(new GID())), target);
	}

	public FriendByNameRequestHandler() {
		super(FriendByNameRequest::new);
	}
}
