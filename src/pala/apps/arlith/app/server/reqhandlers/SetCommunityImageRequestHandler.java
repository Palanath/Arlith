package pala.apps.arlith.app.server.reqhandlers;

import java.io.IOException;

import pala.apps.arlith.api.communication.protocol.errors.InvalidOptionError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.api.communication.protocol.requests.SetCommunityImageRequest;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.app.server.contracts.world.ServerCommunity;
import pala.apps.arlith.app.server.contracts.world.ServerObject;
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
					System.err.println("AN ERROR OCCURRED WHILE TRYING TO SET A COMMUNITY ICON:");
					e.printStackTrace();
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
