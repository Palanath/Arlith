package pala.apps.arlith.app.server.reqhandlers;

import java.util.List;

import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.RetrieveMessagesBeforeRequest;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.MessageValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerMessage;
import pala.apps.arlith.app.server.contracts.world.ServerThread;
import pala.libs.generic.JavaTools;

public final class RetrieveMessagesBeforeRequestHandler
		extends SimpleRequestHandler<RetrieveMessagesBeforeRequest> {

	@Override
	protected void handle(final RetrieveMessagesBeforeRequest r, final RequestConnection client)
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

		// TODO Verify integrity of count?
		ServerMessage pivot = thread.getMessageByID(r.getPivot().getGid());
		List<? extends ServerMessage> messages = thread.getPreviousMessages(pivot, r.getCount().getValue());

		client.sendResult(
				new ListValue<>(JavaTools.mask(messages.iterator(), a -> new MessageValue(new TextValue(a.getContent()),
						new GIDValue(a.getAuthor().getGID()), r.getThread(), new GIDValue(a.getGID())))));
	}

	public RetrieveMessagesBeforeRequestHandler() {
		super(RetrieveMessagesBeforeRequest::new);
	}
}
