package pala.apps.arlith.app.server.systems;

import pala.apps.arlith.api.communication.gids.GID;
import pala.apps.arlith.api.connections.networking.Connection;
import pala.apps.arlith.app.server.contracts.serversystems.EventConnection;

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
