package pala.apps.arlith.backend.server.reqhandlers;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.ListFriendsRequest;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.JavaTools;

public final class ListFriendsRequestHandler extends SimpleRequestHandler<ListFriendsRequest> {

	@Override
	protected void handle(final ListFriendsRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		client.sendResult(new ListValue<UserValue>(
				JavaTools.mask(client.getUser().getFriends().iterator(), RequestHandlerUtils::fromUser)));
	}

	public ListFriendsRequestHandler() {
		super(ListFriendsRequest::new);
	}
}
