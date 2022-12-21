package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.GetCommunityOwnerRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerCommunity;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

public final class GetCommunityOwnerRequestHandler extends SimpleRequestHandler<GetCommunityOwnerRequest> {

	@Override
	protected void handle(final GetCommunityOwnerRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final ServerCommunity community = client.getUser().getJoinedCommunityByID(r.getId().getGid());
		if (community == null)
			client.sendError(new ObjectNotFoundError(r.getId()));
		else
			client.sendResult(new GIDValue(community.getOwner().getGID()));
	}

	public GetCommunityOwnerRequestHandler() {
		super(GetCommunityOwnerRequest::new);
	}
}
