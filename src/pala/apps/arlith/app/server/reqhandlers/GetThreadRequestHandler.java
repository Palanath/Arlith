package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.GetThreadRequest;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.communication.protocol.types.ThreadValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerCommunityThread;
import pala.apps.arlith.app.server.contracts.world.ServerThread;

public final class GetThreadRequestHandler extends SimpleRequestHandler<GetThreadRequest> {

	@Override
	protected void handle(final GetThreadRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerThread thread = client.getUser().getAccessibleThreadByID(r.getThread().getGid());
		if (thread == null) {
			client.sendError(new ObjectNotFoundError());
			return;
		}

		// Modify ThreadValue so that it supports
		client.sendResult(new ThreadValue(
				new TextValue(thread instanceof ServerCommunityThread ? ((ServerCommunityThread) thread).getName() : ""),
				new GIDValue(thread.getGID())));
	}

	public GetThreadRequestHandler() {
		super(GetThreadRequest::new);
	}
}
