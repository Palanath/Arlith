package pala.apps.arlith.app.server.reqhandlers;

import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.ListFriendsRequest;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.UserValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
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
