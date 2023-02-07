package pala.apps.arlith.backend.client;

import java.net.Socket;

import pala.apps.arlith.backend.client.events.EventSubsystem;
import pala.apps.arlith.backend.client.events.StandardEventReader;
import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.common.protocol.requests.AuthRequest;
import pala.apps.arlith.backend.common.protocol.types.BooleanValue;
import pala.apps.arlith.libraries.networking.Communicator;
import pala.apps.arlith.libraries.networking.Connection;

abstract class StandardEventSubsystem extends EventSubsystem {
	private final AuthToken token;

	public StandardEventSubsystem(AuthToken token) {
		super(new StandardEventReader());
		this.token = token;
	}

	@Override
	protected Connection prepareConnection() throws InterruptedException, Exception {
		Communicator connection = new Communicator(prepareSocket());
		AuthRequest req = new AuthRequest(token);
		req.setEventConnection(BooleanValue.TRUE);
		req.inquire(connection);
		return connection;
	}

	/**
	 * Prepares an active {@link Socket} connected to the server for use by this
	 * {@link StandardEventSubsystem}. This method should not communicate over the
	 * {@link Socket}; it should simply parameterize it and open the connection,
	 * then return it. This {@link StandardEventSubsystem}'s
	 * {@link #prepareConnection()} method will call this method and then perform
	 * the actual log-in handshake with the server (as well as establishing
	 * encryption and other facilities).
	 * 
	 * @return A new {@link Socket} that has been parameterized to connect to the
	 *         right endpoint and opened.
	 * @throws InterruptedException If the {@link Thread} is interrupted while
	 *                              preparing the {@link Socket}.
	 * @throws Exception            If any networking or other errors occur. This
	 *                              will cause the method to be reinvoked,
	 *                              potentially after a certain delay, so that the
	 *                              connection can be reattempted.
	 */
	protected abstract Socket prepareSocket() throws InterruptedException, Exception;

}