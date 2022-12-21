package pala.apps.arlith.app.client.api.notifs;

import pala.apps.arlith.app.client.Client;
import pala.apps.arlith.backend.communication.gids.GID;

public class ClientDirectMessageNotification extends ClientNotification {
	private final GID msg, thread;

	public ClientDirectMessageNotification(GID gid, Client client, GID msg, GID thread) {
		super(gid, client);
		this.msg = msg;
		this.thread = thread;
	}

	public GID getMsg() {
		return msg;
	}

	public GID getThread() {
		return thread;
	}

}
