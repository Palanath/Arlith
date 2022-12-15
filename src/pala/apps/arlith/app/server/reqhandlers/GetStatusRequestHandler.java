package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.GetStatusRequest;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;

public final class GetStatusRequestHandler extends SimpleRequestHandler<GetStatusRequest> {

	@Override
	protected void handle(final GetStatusRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerUser user = client.getWorld().getUserByID(r.getUser().getGid());
		if (user == null)
			client.sendError(new ObjectNotFoundError());
		else
			// Statuses are not currently set up on the server's end.
			client.sendResult(NULL/* user.getStatus()==null?NULL:new TextValue(user.getStatus()) */);
	}

	public GetStatusRequestHandler() {
		super(GetStatusRequest::new);
	}
}
