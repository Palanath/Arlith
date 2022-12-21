package pala.apps.arlith.frontend.server.reqhandlers;

import java.util.function.Function;

import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetIncomingFriendRequestsRequest;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.ListValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;
import pala.libs.generic.JavaTools;

public final class GetIncomingFriendRequestsRequestHandler
		extends SimpleRequestHandler<GetIncomingFriendRequestsRequest> {

	@Override
	protected void handle(final GetIncomingFriendRequestsRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		// Check if client is authorized.
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		client.sendResult(new ListValue<>(JavaTools.mask(client.getUser().getIncomingFriendRequestUsers().iterator(),
				(Function<ServerUser, GIDValue>) t -> new GIDValue(t.getGID()))));
	}

	public GetIncomingFriendRequestsRequestHandler() {
		super(GetIncomingFriendRequestsRequest::new);
	}
}
