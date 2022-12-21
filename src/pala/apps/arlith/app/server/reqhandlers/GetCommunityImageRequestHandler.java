package pala.apps.arlith.app.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.world.ServerCommunity;
import pala.apps.arlith.backend.communication.protocol.errors.InvalidOptionError;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.requests.GetCommunityImageRequest;
import pala.apps.arlith.backend.communication.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.backend.streams.InputStream;

public final class GetCommunityImageRequestHandler extends SimpleRequestHandler<GetCommunityImageRequest> {

	@Override
	protected void handle(GetCommunityImageRequest r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException {
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {
			final ServerCommunity comm = client.getUser().getJoinedCommunityByID(r.getCommunity().getGid());
			if (comm == null) {
				client.sendError(new ObjectNotFoundError(r.getCommunity()));
				return;
			}

			// TODO Fix from here down.
			final InputStream icon;
			try {
				switch (r.getType().getValue()) {
				case "icon":
					icon = comm.getIcon();
					break;
				case "background-image":
					icon = comm.getBackground();
					break;
				default:
					client.sendError(new InvalidOptionError(new TextValue("type")));
					return;
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			// If icon is not present, we want to send a PieceOMediaValue with null as an
			// argument to the constructor.
			//
			// Also, the icon variable will be null by here if the icon is not present
			// (check documentation for getIcon() and getBackground()).
			if (icon == null)
				client.sendResult(NULL);
			else
				try {
					client.sendResult(new PieceOMediaValue(icon));
				} catch (UnknownCommStateException | IOException e) {
					throw new RuntimeException(e);
				} finally {
					try {
						icon.close();// Close the icon so that the file isn't locked.
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
		}
	}

	public GetCommunityImageRequestHandler() {
		super(GetCommunityImageRequest::new);
	}
}
