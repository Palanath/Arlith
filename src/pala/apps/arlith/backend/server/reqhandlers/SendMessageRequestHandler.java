package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.requests.SendMessageRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerMessage;
import pala.apps.arlith.backend.server.contracts.world.ServerThread;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.JavaTools;

public final class SendMessageRequestHandler extends SimpleRequestHandler<SendMessageRequest> {

	@Override
	protected void handle(final SendMessageRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerThread thread = client.getUser().getAccessibleThreadByID(r.getThread().getGid());
		if (thread == null) {
			client.sendError(new ObjectNotFoundError(r.getThread()));
			return;
		}
		ServerMessage m = thread.sendMessage(r.getContent().getValue(), client.getUser());
		MessageValue response = RequestHandlerUtils.fromMessage(m);
		client.sendResult(response);

		MessageCreatedEvent event = new MessageCreatedEvent(response, new GIDValue(new GID()));
		client.getServer().getEventSystem().fire(event, client.getUserID(),
				JavaTools.mask(thread.getParticipants(), ServerUser::getGID));
	}

	public SendMessageRequestHandler() {
		super(SendMessageRequest::new);
	}
}
