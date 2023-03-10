package pala.apps.arlith.backend.server.reqhandlers;

import java.io.FileNotFoundException;
import java.io.IOException;

import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.requests.GetProfileIconRequest;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.streams.InputStream;

public final class GetProfileIconRequestHandler extends SimpleRequestHandler<GetProfileIconRequest> {

	@Override
	protected void handle(final GetProfileIconRequest r, final RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			final ServerUser user = client.getWorld().getUserByID(r.getUser().getGid());
			if (user == null) {
				client.sendError(new ObjectNotFoundError(r.getUser()));
				return;
			}

			InputStream icon;
			try {
				icon = user.getProfileIcon();
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}

			// We send over NULL (the null protocol value) if the user does not have an
			// icon. The corresponding Request needs to parse the return value of the
			// request accordingly.
			if (icon == null)
				client.sendResult(NULL);
			else
				try {
					client.sendResult(new PieceOMediaValue(icon));
				} catch (final IOException e) {
					throw new RuntimeException(e);
				} finally {
					try {
						icon.close();
					} catch (IOException e) {
						ArlithServer.getThreadLogger().err(e);
					}
				}
		}
	}

	public GetProfileIconRequestHandler() {
		super(GetProfileIconRequest::new);
	}
}
