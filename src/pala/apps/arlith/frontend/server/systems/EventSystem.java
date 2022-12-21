package pala.apps.arlith.frontend.server.systems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import pala.apps.arlith.application.Logging;
import pala.apps.arlith.backend.communication.gids.GID;
import pala.apps.arlith.backend.communication.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.EventConnection;
import pala.apps.arlith.frontend.server.contracts.world.ServerUser;

public class EventSystem {

	private final Map<GID, List<EventConnection>> clients = new HashMap<>();

	/**
	 * Gets all of the {@link EventConnection}s logged in as the {@link ServerUser}
	 * given by the specified {@link GID}.
	 * 
	 * @param user The {@link GID} of the user to get the event clients of.
	 * @return An unmodifiable {@link List} of the event clients.
	 */
	public List<EventConnection> getClients(GID user) {
		return clients.containsKey(user) ? Collections.unmodifiableList(clients.get(user)) : Collections.emptyList();
	}

	// TODO Check if the exceptions raised when closed EventConnections are used is
	// an UnknownCommState or an IOException.
	/**
	 * <p>
	 * Registers the specified {@link EventConnection} under the user that's
	 * connected. This causes the {@link EventSystem} to "track" the specified
	 * {@link EventConnection} (so that calls to {@link #getClients(GID)} and any of
	 * the appropriate {@link #fire(CommunicationProtocolEvent, GID)} methods will target the
	 * {@link EventConnection}). The {@link EventConnection} can be unregistered any
	 * time after (without closing it) through
	 * {@link #unregisterClient(EventConnection)} or
	 * {@link #unregisterAllClients(GID)}.
	 * </p>
	 * <p>
	 * This method <span style="color: red;">requires</span> the
	 * {@link EventConnection} to be authorized, so that calling
	 * {@link EventConnection#getUserID()} returns a valid {@link GID}.
	 * </p>
	 * <p>
	 * Note that, when firing an event, if an {@link UnknownCommStateException}
	 * occurs, the {@link EventSystem} will automatically close the
	 * {@link EventConnection} (and unregister it).
	 * </p>
	 * <p>
	 * Another thing to note is that it is possible to
	 * {@link EventConnection#closeConnection() close} the event connection despite
	 * it being registered in an {@link EventSystem}. When this happens, attempts to
	 * fire an event to the {@link EventConnection} (invoked through request
	 * handlers) will cause the {@link EventConnection} to raise an exception. It is
	 * recommended to unregister an {@link EventConnection} before closing it (just
	 * in case another thread attempts to fire an event on it). That said, under
	 * synchronized or controlled conditions, it is entirely possible for a closed
	 * {@link EventConnection} to exist inside an {@link EventSystem} without
	 * issues.
	 * </p>
	 * 
	 * @param client The {@link EventConnection} to register. The user's {@link GID}
	 *               is obtained by calling {@link EventConnection#getUserID()}.
	 */
	public void registerClient(EventConnection client) {
		List<EventConnection> clients;
		GID user = client.getUserID();
		if (this.clients.containsKey(user))
			clients = this.clients.get(user);
		else
			this.clients.put(user, clients = new ArrayList<>());

		clients.add(client);
	}

	/**
	 * Unregisters an {@link EventConnection}. This needs to be done manually, by
	 * whatever code using this {@link EventSystem} API, unless the
	 * {@link EventConnection} encounters an {@link UnknownCommStateException}, in
	 * which case the {@link EventConnection} will be terminated (and unregistered)
	 * by this {@link EventSystem} automatically.
	 * 
	 * @param client The {@link EventConnection} to unregister.
	 */
	public void unregisterClient(EventConnection client) {
		if (clients.containsKey(client.getUserID()))
			clients.get(client.getUserID()).remove(client);
	}

	/**
	 * Unregisters all of the registered {@link EventConnection}s for the specified
	 * user {@link GID}.
	 * 
	 * @param user The {@link GID} of the user to unregister all the
	 *             {@link EventConnection}s for.
	 */
	public void unregisterAllClients(GID user) {
		clients.remove(user);
	}

	/**
	 * <p>
	 * Fires an event across the specified {@link EventConnection}. This method is
	 * designed to be called with {@link EventConnection}s that are currently
	 * registered, although it does not have to be.
	 * </p>
	 * <p>
	 * This method sends the specified {@link CommunicationProtocolEvent} over the specified
	 * {@link EventConnection}. If the {@link EventConnection#sendEvent(CommunicationProtocolEvent)}
	 * call completes normally (no exception), then this method simply returns. If
	 * the call does not complete normally and an {@link UnknownCommStateException}
	 * is thrown, then this method attempts to
	 * {@link #unregisterClient(EventConnection) unregister} the specified
	 * {@link EventConnection} from this {@link EventSystem}. (If it is not
	 * registered, then that operation does nothing.) Then it attempts to close the
	 * {@link EventConnection} using {@link EventConnection#closeConnection()
	 * closeConnection(}.
	 * </p>
	 * 
	 * @param event  The {@link CommunicationProtocolEvent} to send.
	 * @param client The {@link EventConnection} to send it over.
	 */
	public void fire(CommunicationProtocolEvent event, EventConnection client) {
		try {
			client.sendEvent(event);
		} catch (UnknownCommStateException e) {
			unregisterClient(client);
			client.closeConnection();
		}
	}

	/**
	 * Fires the provided event amongst all of the clients connected as the provided
	 * {@link ServerUser}.
	 * 
	 * @param event The event to fire.
	 * @param user  The user of which all of the clients connected as, the event
	 *              shall be fired amongst. English 100.
	 */
	public void fire(CommunicationProtocolEvent event, ServerUser user) {
		fire(event, user.getGID());
	}

	public void fire(CommunicationProtocolEvent event, GID user) {
		Exception ex = null;// TODO Have an event handling mechanism set up.

		List<EventConnection> clients = getClients(user);
		for (EventConnection c : clients)
			try {
				fire(event, c);
			} catch (Exception e) {
				Logging.err("Failed to fire an event to the client: " + c + '.');
				if (ex == null)
					ex = e;
				else
					ex.addSuppressed(e);
			}
	}

	/**
	 * <p>
	 * Fires the specified {@link CommunicationProtocolEvent} to all the {@link EventConnection}s of
	 * all the users specified in the array of user {@link GID}s except for the
	 * {@link EventConnection}s of the specified invoker user {@link GID}.
	 * </p>
	 * <p>
	 * This is designed to be called for event firing scenarios where, e.g., one
	 * user sends a message in a thread. All <i>other</i> users in the thread need
	 * to be notified, but the user that sent the message (causing the event to be
	 * fired) does not.
	 * </p>
	 * 
	 * @param event   The {@link CommunicationProtocolEvent} to send.
	 * @param invoker The {@link GID} of the user to <b>not</b> notify. This same
	 *                {@link GID} can be contained inside the array of users; the
	 *                user will <b>still</b> not be notified. This parameter can be
	 *                <code>null</code> in which none of the users in the users
	 *                array are ignored.
	 * @param users   The array of users to fire the event to. Any entry in this
	 *                array that is equal to the specified invoker {@link GID} will
	 *                be ignored. Duplicates otherwise will have events fired for
	 *                them twice.
	 */
	public void fire(CommunicationProtocolEvent event, GID invoker, GID... users) {
		Exception ex = null;
		for (GID g : users)
			if (!Objects.equals(g, invoker))
				try {
					fire(event, g);
				} catch (Exception e) {
					Logging.err("Failed to fire an event to the user: " + g + '.');
					if (ex == null)
						ex = e;
					else
						ex.addSuppressed(e);
				}
	}

	public void fire(CommunicationProtocolEvent event, GID... users) {
		Exception ex = null;
		for (GID g : users)
			try {
				fire(event, g);
			} catch (Exception e) {
				Logging.err("Failed to fire an event to the user: " + g + '.');
				if (ex == null)
					ex = e;
				else
					ex.addSuppressed(e);
			}
	}

	public void fire(CommunicationProtocolEvent event, GID invoker, Iterable<GID> users) {
		Exception ex = null;
		for (GID g : users)
			if (!Objects.equals(g, invoker))
				try {
					fire(event, g);
				} catch (Exception e) {
					Logging.err("Failed to fire an event to the user: " + g + '.');
					if (ex == null)
						ex = e;
					else
						ex.addSuppressed(e);
				}
	}

	public void fire(CommunicationProtocolEvent event, Iterable<GID> users) {
		Exception ex = null;
		for (GID g : users)
			try {
				fire(event, g);
			} catch (Exception e) {
				Logging.err("Failed to fire an event to the user: " + g + '.');
				if (ex == null)
					ex = e;
				else
					ex.addSuppressed(e);
			}
	}

}
