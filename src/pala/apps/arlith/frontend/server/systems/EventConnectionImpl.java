package pala.apps.arlith.frontend.server.systems;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.connections.networking.Connection;
import pala.apps.arlith.frontend.server.contracts.serversystems.EventConnection;

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
