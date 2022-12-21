package pala.apps.arlith.frontend.server.reqhandlers;

import java.util.List;

import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.RetrieveMessagesRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerMessage;
import pala.apps.arlith.frontend.server.contracts.world.ServerThread;
import pala.libs.generic.JavaTools;

public final class RetrieveMessagesRequestHandler extends SimpleRequestHandler<RetrieveMessagesRequest> {

	@Override
	protected void handle(final RetrieveMessagesRequest r, final RequestConnection client)
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
		List<? extends ServerMessage> messages = thread.getLatestMessages(r.getCount().getValue());
		// TODO Set constraint on message count.
		final ListValue<MessageValue> commMessages = new ListValue<>(
				JavaTools.mask(messages.iterator(), a -> new MessageValue(new TextValue(a.getContent()),
						new GIDValue(a.getAuthor().getGID()), new GIDValue(thread.getGID()), new GIDValue(a.getGID()))));
		client.sendResult(commMessages);
	}

	public RetrieveMessagesRequestHandler() {
		super(RetrieveMessagesRequest::new);
	}
}
