package pala.apps.arlith.backend.client.api.notifs;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.SimpleClientObject;
import pala.apps.arlith.backend.common.gids.GID;

public class ClientNotification extends SimpleClientObject {

	public ClientNotification(GID gid, ArlithClient client) {
		super(gid, client);
	}

}
