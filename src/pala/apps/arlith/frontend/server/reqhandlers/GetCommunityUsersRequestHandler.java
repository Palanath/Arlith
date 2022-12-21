package pala.apps.arlith.frontend.server.reqhandlers;

import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetCommunityUsersRequest;
import pala.apps.arlith.backend.communication.protocol.types.ListValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerCommunity;
import pala.libs.generic.JavaTools;

public final class GetCommunityUsersRequestHandler extends SimpleRequestHandler<GetCommunityUsersRequest> {

	@Override
	protected void handle(final GetCommunityUsersRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerCommunity cmnty = client.getUser().getJoinedCommunityByID(r.getCommunity().getGid());
		if (cmnty == null) {
			client.sendError(new ObjectNotFoundError());
			return;
		}
		// Send over a ListValue of all the community members.
		client.sendResult(new ListValue<>(JavaTools.mask(cmnty.getUsers().iterator(), RequestHandlerUtils::fromUser)));
	}

	public GetCommunityUsersRequestHandler() {
		super(GetCommunityUsersRequest::new);
	}
}
