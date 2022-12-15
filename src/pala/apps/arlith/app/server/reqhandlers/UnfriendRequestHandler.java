package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.UnfriendRequest;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;

public final class UnfriendRequestHandler extends SimpleRequestHandler<UnfriendRequest> {

	@Override
	protected void handle(final UnfriendRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerUser target = client.getWorld().getUserByID(r.getUser().getGid());
		if (target == null) {
			client.sendError(new ObjectNotFoundError(r.getUser()));
			return;
		} else if (target.getGID() == client.getUserID()) {
			client.sendError(new ObjectNotFoundError());
			return;
		}
		// TODO Make unfriend return a boolean so we can know when to fire an event.
		client.getUser().unfriend(target);
		client.sendResult(new CompletionValue());
	}

	public UnfriendRequestHandler() {
		super(UnfriendRequest::new);
	}
}
