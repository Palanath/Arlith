package pala.apps.arlith.frontend.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.CreateCommunityRequest;
import pala.apps.arlith.backend.communication.protocol.types.CommunityValue;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.ListValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerCommunity;

public final class CreateCommunityRequestHandler extends SimpleRequestHandler<CreateCommunityRequest> {

	@Override
	protected void handle(CreateCommunityRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			ServerCommunity sc = client.getUser().createCommunity(r.getName().getValue());
			if (r.hasIcon())
				try {
					sc.setIcon(r.getIcon().getMedia());
				} catch (IOException e) {
					e.printStackTrace();
					throw new UnknownCommStateException(
							"An unknown error occurred that may have affected the connection, so it was terminated.");
				}
			if (r.hasBackground())
				try {
					sc.setBackground(r.getBackground().getMedia());
				} catch (IOException e) {
					e.printStackTrace();
					throw new UnknownCommStateException(
							"An unknown error occurred that may have affected the connection, so it was terminated.");
				}

			// Send the newly created community over to the client.
			client.sendResult(new CommunityValue(new TextValue(sc.getName()), new ListValue<>(),
					new ListValue<>(new GIDValue(client.getUserID())), new GIDValue(sc.getGID())));
		}
	}

	public CreateCommunityRequestHandler() {
		super(a -> {
			// TODO Fix
			return new CreateCommunityRequest(a, null);
		});
	}
}
