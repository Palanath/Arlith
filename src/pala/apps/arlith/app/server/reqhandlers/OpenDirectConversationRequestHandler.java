package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.events.ThreadAccessGainedEvent;
import pala.apps.arlith.api.communication.protocol.requests.OpenDirectConversationRequest;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerDirectThread;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.apps.arlith.app.server.contracts.world.ServerUser.FriendState;

public final class OpenDirectConversationRequestHandler extends SimpleRequestHandler<OpenDirectConversationRequest> {

	@Override
	protected void handle(final OpenDirectConversationRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		// No PMing oneself.
		if (r.getRecipient().getGid().equals(client.getUserID())) {
			client.sendError(new ObjectNotFoundError());
			return;
		}
		final ServerUser recip = client.getWorld().getUserByID(r.getRecipient().getGid());
		// No PMing invalid users.
		if (recip == null) {
			client.sendError(new ObjectNotFoundError(r.getRecipient()));
			return;
		}
		if (client.getUser().getFriendState(recip) != FriendState.FRIENDED) {
			client.sendError(new AccessDeniedError());
			return;
		}
		ServerDirectThread thread;
		thread = client.getUser().openDirectThread(recip);
		client.getServer().getEventSystem().fire(new ThreadAccessGainedEvent(new GIDValue(thread.getGID())),
				recip.getGID());
		client.sendResult(new GIDValue(thread.getGID()));
	}

	public OpenDirectConversationRequestHandler() {
		super(OpenDirectConversationRequest::new);
	}
}
