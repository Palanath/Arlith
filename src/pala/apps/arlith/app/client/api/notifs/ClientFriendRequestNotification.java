package pala.apps.arlith.app.client.api.notifs;

import pala.apps.arlith.app.client.ArlithClient;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.types.FriendStateValue;

public class ClientFriendRequestNotification extends ClientNotification {
	private final GID sourceUser;
	private final FriendStateValue oldState, newState;

	public ClientFriendRequestNotification(GID gid, ArlithClient client, GID sourceUser, FriendStateValue oldState,
			FriendStateValue newState) {
		super(gid, client);
		this.sourceUser = sourceUser;
		this.oldState = oldState;
		this.newState = newState;
	}

	public GID getSourceUser() {
		return sourceUser;
	}

	public FriendStateValue getOldState() {
		return oldState;
	}

	public FriendStateValue getNewState() {
		return newState;
	}

}
