package pala.apps.arlith.backend.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.CreateCommunityRequest;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerCommunity;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

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
					ArlithServer.getThreadLogger().err(e);
					throw new UnknownCommStateException(
							"An unknown error occurred that may have affected the connection, so it was terminated.");
				}
			if (r.hasBackground())
				try {
					sc.setBackground(r.getBackground().getMedia());
				} catch (IOException e) {
					ArlithServer.getThreadLogger().err(e);
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
