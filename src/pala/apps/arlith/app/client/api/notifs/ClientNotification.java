package pala.apps.arlith.app.client.api.notifs;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.app.client.Client;
import pala.apps.arlith.app.client.api.SimpleClientObject;

public class ClientNotification extends SimpleClientObject {

	public ClientNotification(GID gid, Client client) {
		super(gid, client);
	}

}
