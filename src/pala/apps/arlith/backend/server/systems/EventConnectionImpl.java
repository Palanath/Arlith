package pala.apps.arlith.backend.server.systems;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.server.contracts.serversystems.EventConnection;

public class EventConnectionImpl implements EventConnection {

	private Connection connection;
	private final GID userID;

	public EventConnectionImpl(Connection connection, GID userID) {
		this.connection = connection;
		this.userID = userID;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public GID getUserID() {
		return userID;
	}

}
