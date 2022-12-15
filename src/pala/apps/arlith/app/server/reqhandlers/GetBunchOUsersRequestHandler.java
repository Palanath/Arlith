package pala.apps.arlith.app.server.reqhandlers;

import java.util.ArrayList;
import java.util.List;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.requests.GetBunchOUsersRequest;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.UserValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerUser;
import pala.libs.generic.JavaTools;

public final class GetBunchOUsersRequestHandler extends SimpleRequestHandler<GetBunchOUsersRequest> {

	@Override
	protected void handle(final GetBunchOUsersRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized()) {
			client.sendError(new RestrictedError("Client not authorized to perform that action."));
			return;
		}
		final List<UserValue> users = new ArrayList<>(r.getUsers().size());
		for (final GID id : JavaTools.mask(r.getUsers(), GIDValue::getGid)) {
			final ServerUser user = client.getWorld().getUserByID(id);
			if (user == null) {
				client.sendError(new ObjectNotFoundError(new GIDValue(id)));
				return;
			}
			users.add(RequestHandlerUtils.fromUser(user));
		}
		client.sendResult(new ListValue<>(users));
	}

	public GetBunchOUsersRequestHandler() {
		super(GetBunchOUsersRequest::new);
	}
}
