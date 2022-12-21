package pala.apps.arlith.backend.server;

import java.io.File;
import java.net.Socket;

import pala.apps.arlith.backend.common.authentication.AuthToken;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.Communicator;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.networking.scp.CommunicationConnectionAcceptor;
import pala.apps.arlith.backend.server.contracts.serversystems.EventConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestMapper;
import pala.apps.arlith.backend.server.contracts.world.ServerWorld;
import pala.apps.arlith.backend.server.systems.AuthenticationSystem;
import pala.apps.arlith.backend.server.systems.EventSystem;
import pala.apps.arlith.backend.server.systems.RequestSystemImpl;
import pala.apps.arlith.backend.server.world.ServerWorldImpl;

public class ArlithServer extends CommunicationConnectionAcceptor {

	private final ServerWorld world = new ServerWorldImpl(new File("arlith-data"), this);
	private final RequestSystemImpl requestManager = new RequestSystemImpl(this);
	/**
	 * Manages all forms of user authentication in the application. This can be
	 * queried to check if a user provided the right {@link AuthToken} or log-in
	 * information when authenticating a connection.
	 */
	private final AuthenticationSystem authSystem = new AuthenticationSystem();
	/**
	 * Tracks all of the {@link EventConnection}s that are logged in. Every
	 * {@link EventConnection} that is logged in under a certain user is tracked.
	 * This object also allows convenient means of firing events to
	 * {@link EventConnection}s and multiple users at the same time.
	 */
	private final EventSystem eventSystem = new EventSystem();

	@Override
	protected void acceptSocket(Socket incomingSocketConnection) throws Exception {
		// This is called on a new thread (unless
		// CommunicationConnectionAcceptor#acceptOnNewThread is set to false manually).

		System.out.println(
				'[' + incomingSocketConnection.getInetAddress().getHostAddress() + "] connection established.");

		Communicator communicator = new Communicator(incomingSocketConnection);

		final RequestConnection connection = requestManager.new RequestConnectionImpl(communicator);
		while (connection.active())
			try {
				requestManager.handleRequest(connection);
			} catch (ClassCastException | MalformedIncomingRequestException | RequestNotSupportedException e) {
				e.printStackTrace();

			} catch (UnknownCommStateException | BlockException e) {
				// Connection issue or connection reset.
				System.out
						.println('[' + incomingSocketConnection.getInetAddress().getHostAddress() + "] disconnected. ");
				communicator.close();
				break;
			}

		// Unregister it if it was registered by an authorization request handler.
		requestManager.unregisterRequestClient(connection);
	}

	@Override
	protected void acceptConnection(final Connection communicator) {
		// Let #acceptSocket(Socket) handle.
	}

	public AuthenticationSystem getAuthSystem() {
		return authSystem;
	}

	public EventSystem getEventSystem() {
		return eventSystem;
	}

	public RequestMapper getRequestManager() {
		return requestManager;
	}

	public ServerWorld getWorld() {
		return world;
	}

}
