package pala.apps.arlith.backend.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.backend.common.protocol.errors.InvalidOptionError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.backend.common.protocol.requests.SetCommunityImageRequest;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.server.ArlithServer;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.backend.server.contracts.world.ServerCommunity;
import pala.apps.arlith.backend.server.contracts.world.ServerObject;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONObject;

public final class SetCommunityImageRequestHandler implements RequestHandler {

	@Override
	public void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException {
		SetCommunityImageRequest r = new SetCommunityImageRequest(request, client.getConnection());
		if (!client.isAuthorized())
			client.sendError(new RestrictedError());
		else {

			// TODO Verify that file is image.
			ServerCommunity c = client.getUser().getJoinedCommunityByID(r.getCommunity().getGid());

			// The media needs to be read out of the connection EVEN if it does not get
			// uploaded (because it's still sitting on the network line).

			if (c == null)
				client.sendError(new ObjectNotFoundError(r.getCommunity()));
			else {
				// Check user permissions.
				if (!c.getOwner().equals(client.getUser())) {
					// User needs to be the owner to be able to change the icon for now.
					client.sendError(new RestrictedError());
					return;
				}
				try {
					switch (r.getType().getValue()) {
					case "icon":
						c.setIcon(r.getImage().getMedia());
						break;
					case "background-image":
						c.setBackground(r.getImage().getMedia());
						break;
					default:
						client.sendError(new InvalidOptionError(new TextValue("type")));
						return;
					}
				} catch (IOException | UnknownCommStateException | BlockException e) {
					ArlithServer.getThreadLogger().err("AN ERROR OCCURRED WHILE TRYING TO SET A COMMUNITY ICON:");
					ArlithServer.getThreadLogger().err(e);
					client.sendError(new ServerError());
					return;
				}
				client.sendResult(new CompletionValue());
				LazyCommunityImageChangedEvent event = new LazyCommunityImageChangedEvent(new GIDValue(c.getGID()),
						new TextValue(r.getType().getValue()));
				// TODO Synchronize
				client.getServer().getEventSystem().fire(event, client.getUserID(),
						JavaTools.mask(JavaTools.iterable(c.getUsers()), ServerObject::getGID));
			}
		}
	}
}
