package pala.apps.arlith.app.client.api.notifs;

import pala.apps.arlith.app.client.ArlithClient;
import pala.apps.arlith.app.client.api.SimpleClientObject;
import pala.apps.arlith.backend.communication.gids.GID;

public class ClientNotification extends SimpleClientObject {

	public ClientNotification(GID gid, ArlithClient client) {
		super(gid, client);
	}

}
