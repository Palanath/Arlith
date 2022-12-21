package pala.apps.arlith.frontend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ListJoinedCommunitiesRequest;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.libs.generic.JavaTools;

public final class ListJoinedCommunitiesRequestHandler
		extends SimpleRequestHandler<ListJoinedCommunitiesRequest> {

	@Override
	protected void handle(final ListJoinedCommunitiesRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		client.sendResult(new ListValue<>(JavaTools.mask(client.getUser().getJoinedCommunities().iterator(), a -> {
			return new CommunityValue(new TextValue(a.getName()),
					new ListValue<>(JavaTools.mask(a.getThreads().iterator(),
							b -> new ThreadValue(new TextValue(b.getName()), new GIDValue(b.getGID())))),
					new ListValue<>(JavaTools.mask(a.getUsers().iterator(), c -> new GIDValue(c.getGID()))),
					new GIDValue(a.getGID()));
		})));
	}

	public ListJoinedCommunitiesRequestHandler() {
		super(ListJoinedCommunitiesRequest::new);
	}
}
