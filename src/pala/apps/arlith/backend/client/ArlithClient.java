package pala.apps.arlith.backend.client;

import static pala.apps.arlith.libraries.CompletableFutureUtils.getValueWithDefaultExceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import pala.apps.arlith.application.logging.Logger;
import pala.apps.arlith.application.logging.LoggingUtilities;
import pala.apps.arlith.backend.client.api.ClientCommunity;
import pala.apps.arlith.backend.client.api.ClientOwnUser;
import pala.apps.arlith.backend.client.api.ClientThread;
import pala.apps.arlith.backend.client.api.ClientUser;
import pala.apps.arlith.backend.client.api.caching.v2.ListCache;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.client.api.notifs.ClientDirectMessageNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientFriendRequestNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientNotification;
import pala.apps.arlith.backend.client.events.EventSubsystem;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.CommunicationProtocolEvent;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.events.StatusChangedEvent;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.CreateCommunityRequest;
import pala.apps.arlith.backend.common.protocol.requests.FriendByGIDRequest;
import pala.apps.arlith.backend.common.protocol.requests.FriendByNameRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetBunchOUsersRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetIncomingFriendRequestsRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetOutgoingFriendRequestsRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetOwnUserRequest;
import pala.apps.arlith.backend.common.protocol.requests.ListFriendsRequest;
import pala.apps.arlith.backend.common.protocol.requests.ListJoinedCommunitiesRequest;
import pala.apps.arlith.backend.common.protocol.types.CommunityValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.libs.generic.JavaTools;
import pala.libs.generic.events.EventHandler;
import pala.libs.generic.events.EventManager;
import pala.libs.generic.events.EventType;

public class ArlithClient {

	/**
	 * The logger for this {@link ArlithClient}. All of the faculties that the
	 * client invokes should log to this logger. Each client and server have its own
	 * logger to help organize log output on instances running both a client and
	 * server, and to organize output against portions of the program that do not
	 * run under a client or server.
	 */
	private final Logger logger = LoggingUtilities.getConfiguredStandardLogger("CLIENT");

	/**
	 * Gets this {@link ArlithClient}'s {@link Logger}. This {@link Logger} is used
	 * to log standard, error, debug, and warning messages that occur during this
	 * client's execution. It should only be modified in certain contexts, (since
	 * the same returne d{@link Logger} is used for all operations that this
	 * {@link ArlithClient} performs), but can be printed to freely by code invoked
	 * by this client.
	 * 
	 * @return The {@link Logger} that can be used to log information for this
	 *         client.
	 */
	public Logger getLogger() {
		return logger;
	}

	private final EventManager<CommunicationProtocolEvent> eventManager = new EventManager<>();
	private final Map<GID, ClientThread> threads = new HashMap<>();
	private final LinkedHashMap<GID, ClientNotification> notifications = new LinkedHashMap<>();

	{
		eventManager.register(MessageCreatedEvent.MESSAGE_CREATED_EVENT, event -> {
			ClientThread thread = getLoadedThread(event.getMessage().getOwnerThread().getGid());
			if (thread != null)
				thread.receiveMessage(event);
			notifications.put(event.getNotificationID().getGid(),
					new ClientDirectMessageNotification(event.getNotificationID().getGid(), this,
							event.getMessage().id(), event.getMessage().getOwnerThread().getGid()));
		});
	}

	private final Map<GID, ClientUser> users = new HashMap<>();

	{
		eventManager.register(StatusChangedEvent.STATUS_CHANGED_EVENT, event -> {
			ClientUser user = getLoadedUser(event.getUser().getGid());
			if (user != null)
				user.receiveStatusChangeEvent(event);
		});
		eventManager.register(LazyProfileIconChangedEvent.LAZY_PROFILE_ICON_CHANGED_EVENT, event -> {
			ClientUser user = getLoadedUser(event.getUser().getGid());
			if (user != null)
				user.receiveLazyProfileIconChangeEvent(event);
		});
		eventManager.register(LazyCommunityImageChangedEvent.LAZY_COMMUNITY_IMAGE_CHANGED_EVENT, event -> {
			ClientCommunity community = getLoadedCommunity(event.getCommunity().getGid());
			if (community != null)
				community.receiveLazyImageChangedEvent(event);
		});
	}

	private final Map<GID, ClientCommunity> communities = new HashMap<>();
	private final ListCache<ClientCommunity> joinedCommunities;
	private final ListCache<ClientUser> friends, incomingFriends, outgoingFriends;
	private final NewCache<ClientOwnUser> self;

	private boolean running;

	private final EventSubsystem eventSubsystem;
	private final RequestQueue requestQueue;

	/**
	 * Creates an {@link ArlithClient} using the specified {@link EventSubsystem}
	 * and {@link RequestQueue}. The {@link EventSubsystem}'s {@link EventManager}
	 * is set to {@link #eventManager}. Once setup and construction is complete, the
	 * client needs to be started up with a call to {@link #startup()} before it can
	 * be used.
	 * 
	 * @param eventSubsystem   The {@link EventSubsystem} to use for listening for
	 *                         events. The {@link EventSubsystem} is expected to be
	 *                         in a non-started state. It is started up upon a call
	 *                         to {@link #startup()}.
	 * @param requestSubsystem The {@link RequestQueue} to use for making requests.
	 *                         The request subsystem is expected to be in a
	 *                         non-started state. It is started up upon a call to
	 *                         {@link #startup()}.
	 */
	public ArlithClient(EventSubsystem eventSubsystem, RequestQueue requestQueue) {
		this.eventSubsystem = eventSubsystem;
		eventSubsystem.setEventManager(eventManager);
		this.requestQueue = requestQueue;

		// Initialize caches with requestQueue.
		joinedCommunities = new ListCache<>(new ListJoinedCommunitiesRequest(), this::cache, requestQueue);
		friends = new ListCache<>(new ListFriendsRequest(), a -> getUser(a.getId().getGid()), requestQueue);
		incomingFriends = new ListCache<ClientUser>(new GetIncomingFriendRequestsRequest(), a -> getUser(a.getGid()),
				requestQueue);
		outgoingFriends = new ListCache<ClientUser>(new GetOutgoingFriendRequestsRequest(), a -> getUser(a.getGid()),
				requestQueue);

		// Setup for friend events.
		eventManager.register(IncomingFriendEvent.INCOMING_FRIEND_EVENT, event -> {
			ClientUser other = getUser(event.getUser().getGid());
			REMOVE: {
				ListCache<ClientUser> cache;
				switch (event.getPreviousState()) {
				case FRIENDED:
					cache = friends;
					break;
				case INCOMING:
					cache = incomingFriends;
					break;
				default:
					break REMOVE;
				case OUTGOING:
					cache = outgoingFriends;
					break;
				}
				if (cache.isPopulated())
					try {
						cache.get().remove(other);
					} catch (CommunicationProtocolError e) {
						assert false
								: "A Cache threw a CommunicationProtocolError after it was populated. (Caches throw these errors when something goes wrong while attempting to populate. This should not happen.)";
					}
			}

			ListCache<ClientUser> cache;
			switch (event.getNewState()) {
			case FRIENDED:
				cache = friends;
				break;
			case INCOMING:
				cache = incomingFriends;
				break;
			case OUTGOING:
				cache = outgoingFriends;
				break;
			default:
				return;
			}
			if (cache.isPopulated())
				try {
					cache.get().add(other);
				} catch (CommunicationProtocolError e) {
					assert false
							: "A Cache threw a CommunicationProtocolError after it was populated. (Caches throw these errors when something goes wrong while attempting to populate. This should not happen.)";
				}
			notifications.put(event.getNotificationID().getGid(),
					new ClientFriendRequestNotification(event.getNotificationID().getGid(), this,
							event.getUser().getGid(), event.getPreviousState(), event.getNewState()));
		});

		self = new NewCache<>(new GetOwnUserRequest(), a -> {
			ClientOwnUser u = new ClientOwnUser(a.id(), ArlithClient.this, a.username(), a.status(), a.messageCount(),
					a.discriminant());
			if (!users.containsKey(a.id()))
				users.put(a.id(), u);
			return u;
		}, requestQueue);
	}

	/**
	 * <p>
	 * Creates a new Community using the provided name. The icon and background are
	 * <i>both</i> optional. To not provide an icon or background, supply
	 * <code>null</code> for either's <code>byte[]</code> argument.
	 * </p>
	 * 
	 * @param name       The name of the community.
	 * @param icon       The icon data itself, or <code>null</code> if no icon is
	 *                   being supplied. This will be read from once this request
	 *                   gets processed and is actually sent over the network. The
	 *                   {@link pala.apps.arlith.libraries.streams.InputStream}
	 *                   supplied should not be used by other code.
	 * @param background The background data itself, or <code>null</code> if no
	 *                   background is being supplied. This will be read from once
	 *                   this request gets processed and is actually sent over the
	 *                   network. The
	 *                   {@link pala.apps.arlith.libraries.streams.InputStream}
	 *                   should not be used by other code.
	 * @return A {@link CompletableFuture} representing the request.
	 */
	public CompletableFuture<ClientCommunity> createCommunityRequest(String name, byte[] icon, byte[] background) {
		return getRequestQueue()
				.queueFuture(new CreateCommunityRequest(name == null ? null : new TextValue(name),
						icon == null ? null : new PieceOMediaValue(icon),
						background == null ? null : new PieceOMediaValue(background)))
				.thenApply(this::cache).thenApply(t -> {
					joinedCommunities.doIfPopulated(a -> a.add(t));
					return t;
				});
	}

	public ClientCommunity createCommunity(String name, byte[] icon, byte[] background)
			throws ServerError, RestrictedError, RateLimitError, SyntaxError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(createCommunityRequest(name, icon, background));
	}

	<T extends CommunicationProtocolEvent> void fire(EventType<T> type, T event) {
		eventManager.fire(type, event);
	}

	// TODO Add later (but make use of cache).
//	public ActionInterface<ClientUser> getUserRequest(GID id) {
//		return requestSubsystem.action(new GetUserRequest(new GIDValue(id)))
//				.then((Function<UserValue, ClientUser>) this::getUser);
//	}

	public void friend(GID userID)
			throws ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		getValueWithDefaultExceptions(friendRequest(userID), ObjectNotFoundError.class);
	}

	public GID friend(String user, String disc)
			throws ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(friendRequest(user, disc), ObjectNotFoundError.class);
	}

	public CompletableFuture<Void> friendRequest(GID userID) {
		return getRequestQueue().queueFuture(new FriendByGIDRequest(new GIDValue(userID))).thenApply(t -> {
			/*
			 * To maintain cache consistency, now that this user is friended, we remove from
			 * list of incoming friend requests (if they were in the list) and add to list
			 * of friends. If they were not, then we add them to the list of outgoing friend
			 * requests.
			 */
			incomingFriends.doIfPopulated(a -> {
				for (Iterator<ClientUser> iterator = a.iterator(); iterator.hasNext();) {
					ClientUser u = iterator.next();
					if (u.id().equals(userID)) {
						// Promote from incoming friend req to added friend.
						iterator.remove();
						friends.doIfPopulated(b -> b.add(u));
						return;
					}
				}
				/*
				 * If they were not an incoming friend request, they are now an outgoing friend
				 * request:
				 */
				outgoingFriends.doIfPopulated(b -> b.add(getUser(userID)));
			});
			return null;
		});
	}

	public CompletableFuture<GID> friendRequest(String user, String disc) {
		return getRequestQueue().queueFuture(new FriendByNameRequest(new TextValue(user), new TextValue(disc)))
				.thenApply(a -> {
					// Same as friendByGID above
					incomingFriends.doIfPopulated(b -> {
						for (Iterator<ClientUser> iterator = b.iterator(); iterator.hasNext();) {
							ClientUser u = iterator.next();
							if (u.id().equals(a.getGid())) {
								// Promote from incoming friend req to added friend.
								iterator.remove();
								friends.doIfPopulated(c -> c.add(u));
								return;
							}
						}
						outgoingFriends.doIfPopulated(c -> c.add(getUser(a.getGid())));
					});

					return a.getGid();
				});
	}

//	public Set<ClientUser> getBunchOUsers(boolean filter, GID... gids)
//			throws CommunicationProtocolError,  RuntimeException {
//		return getBunchOUsers(JavaTools.iterable(gids), filter);
//	}

	public Set<ClientUser> getBunchOUsers(GID... gids) throws CommunicationProtocolError, RuntimeException {
		return getBunchOUsers(JavaTools.iterable(gids));
	}

	public Set<ClientUser> getBunchOUsers(Iterable<GID> gids)
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getBunchOUsersRequest(gids), AccessDeniedError.class,
				ObjectNotFoundError.class);
	}

	public CompletableFuture<Set<ClientUser>> getBunchOUsersRequest(GID... gids) {
		return getBunchOUsersRequest(JavaTools.iterable(gids));
	}

	/**
	 * <p>
	 * Returns a {@link Set} of {@link ClientUser}s representing the users with IDs
	 * returned by the provided {@link Iterable}.
	 * </p>
	 * <p>
	 * Upon being called, this method immediately collects all the users, whose IDs
	 * are specified, that have already been loaded and are present in the cache.
	 * Then, if all the users specified have been collected, the returned
	 * {@link CompletableFuture} will have already been completed successfully.
	 * Otherwise, the returned {@link CompletableFuture} queries all the users not
	 * already in the cache (all the users this method failed to find), and then
	 * adds those queried to those found in the cache, and returns the result. The
	 * resulting {@link Set} contains exactly all users requested.
	 * </p>
	 * 
	 * @param gids The {@link GID}s of the users to request.
	 * @return An {@link CompletableFuture} representing the request.
	 */
	public CompletableFuture<Set<ClientUser>> getBunchOUsersRequest(Iterable<? extends GID> gids) {
		Set<ClientUser> users = new HashSet<>();
		Set<GID> unqueriedUsers = new HashSet<>();
		for (GID g : gids) {
			ClientUser u;
			synchronized (this.users) {
				u = this.users.get(g);
			}
			if (u != null)
				users.add(u);
			else
				unqueriedUsers.add(g);
		}
		if (unqueriedUsers.isEmpty())
			return CompletableFuture.completedFuture(users);
		else
			return getRequestQueue()
					.queueFuture(new GetBunchOUsersRequest(
							new ListValue<>(JavaTools.mask(unqueriedUsers.iterator(), GIDValue::new))))
					/*
					 * Cache all newly queried users, add them to the `users` set, and then return
					 * the set.
					 * 
					 * Note that this::cache handles "caching conflicts" where, say, two calls to
					 * this method simultaneously query and try to cache two different UserValues
					 * for the same actual user. (It also synchronizes over the cache.)
					 */
					.thenApply(a -> JavaTools.addAll(a, this::cache, users));
	}

	public Collection<ClientThread> getCachedThreads() {
		return Collections.unmodifiableCollection(threads.values());
	}

	public Collection<ClientUser> getCachedUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	/**
	 * <p>
	 * Gets a {@link ClientCommunity} off of a {@link CommunityValue}. This is the
	 * goto conversion function from the Communication Protocol to the Application
	 * Client API regarding communities.
	 * </p>
	 * <p>
	 * This gets the specified community from the cache if it is contained in the
	 * cache. Otherwise, it creates, adds to the cache, and returns the community.
	 * This method should be used for creating and obtaining communities for cache
	 * consistency.
	 * </p>
	 * 
	 * @param c The {@link CommunityValue} to load or get the object of.
	 * @return The (possibly new) {@link ClientCommunity}.
	 */
	public ClientCommunity cache(CommunityValue c) {
		ClientCommunity community;
		synchronized (communities) {
			if ((community = getLoadedCommunity(c.id())) == null)
				communities.put(c.id(), community = new ClientCommunity(c.id(), this, c.getName().getValue(),
						JavaTools.addAll(c.getThreads(), this::cache, new ArrayList<>(c.getThreads().size())),
						JavaTools.addAll(c.getMembers(), GIDValue::getGid, new ArrayList<>(c.getMembers().size()))));
		}
		return community;
	}

	public EventSubsystem getEventSubsystem() {
		return eventSubsystem;
	}

	public List<ClientUser> getIncomingFriendRequests()
			throws ServerError, RestrictedError, RateLimitError, SyntaxError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getIncomingFriendRequestsRequest());
	}

	public CompletableFuture<List<ClientUser>> getIncomingFriendRequestsRequest() {
		return incomingFriends.futureUnmodifiable();
	}

	public ClientCommunity getLoadedCommunity(GID id) {
		return communities.get(id);
	}

	public ClientThread getLoadedThread(GID id) {
		return threads.get(id);
	}

	/**
	 * Gets and returns a {@link ClientUser} that this client keeps track of by the
	 * specified {@link GID}. A user becomes "tracked" if this client receives its
	 * information in an event, or if the result of a request made on this client
	 * supplies this client with its information. The client then stores it locally
	 * and it can be obtained from this method.
	 * 
	 * @param id The {@link GID} of the user to get.
	 * @return The {@link ClientUser}, or <code>null</code> if none by the given id
	 *         is tracked.
	 * @author Palanath
	 */
	public ClientUser getLoadedUser(GID id) {
		return users.get(id);
	}

	public Map<GID, ClientNotification> getNotifications() {
		return Collections.unmodifiableMap(notifications);
	}

	public List<ClientUser> getOutgoingFriendRequests() throws CommunicationProtocolError, RuntimeException {
		return getValueWithDefaultExceptions(getOutgoingFriendRequestsRequest());
	}

	public CompletableFuture<List<ClientUser>> getOutgoingFriendRequestsRequest() {
		return outgoingFriends.futureUnmodifiable();
	}

	public ClientOwnUser getOwnUser() throws CommunicationProtocolError, RuntimeException {
		return getValueWithDefaultExceptions(getOwnUserRequest());
	}

	public CompletableFuture<ClientOwnUser> getOwnUserRequest() {
		return self.future();
	}

	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

	/**
	 * Convenience method to update the cached thread specified by the provided
	 * {@link ThreadValue} (or to cache the thread, if it is not already in the
	 * cache), and then return the cached {@link ClientThread}. If the
	 * {@link ClientThread} is in the cache, its
	 * {@link ClientThread#update(ThreadValue)} method is called. Otherwise, it is
	 * created off the provided {@link ThreadValue}, put in the cache, then
	 * returned.
	 * 
	 * @param thread The {@link ThreadValue} representing the update.
	 * @return The (possibly newly) cached {@link ClientThread} instance.
	 */
	public ClientThread update(ThreadValue thread) {
		ClientThread thrd = cache(thread);
		thrd.update(thread);
		return thrd;
	}

	/**
	 * Used for safely caching the specified {@link ThreadValue}. This method only
	 * updates the cache if the specified thread is not already present in it. This
	 * method will <b>not</b> update the cached thread with the specified
	 * {@link ThreadValue} to permit this method to be safe against race conditions
	 * (i.e., so that two distinct {@link ClientThread} objects are never made for
	 * the same actual thread).
	 * 
	 * @param thread The {@link ThreadValue} representing the thread to cache.
	 * @return The (newly) cached {@link ClientThread} instance.
	 */
	public ClientThread cache(ThreadValue thread) {
		ClientThread thd;
		synchronized (threads) {
			if ((thd = getLoadedThread(thread.id())) == null) {
				thd = new ClientThread(thread, this);
				threads.put(thread.id(), thd);
			}
		}
		return thd;
	}

	public ClientThread getThread(GID id) {
		ClientThread thread;
		synchronized (threads) {
			thread = getLoadedThread(id);
			if (thread == null) {
				thread = new ClientThread(id, this);
				threads.put(id, thread);
			}
		}
		return thread;
	}

	/**
	 * <p>
	 * Gets a {@link ClientUser} off of a {@link UserValue}. This method
	 * synchronizes over the {@link #users user cache} and checks if a user with the
	 * specified GID already exists. If so, it simply returns that user. If not, the
	 * {@link UserValue} provided is used to build a new {@link ClientUser}, which
	 * is then added to the cache and returned.
	 * </p>
	 * <p>
	 * This method works off of the principle that the value of a cache entry should
	 * only be set once, and that any updates to that cache entry will be propagated
	 * from the server to the client, then to the cache, via events. If two calls
	 * to, e.g., {@link #getBunchOUsers(GID...)} take place, and both end up
	 * querying the same user, the second of those two method calls that calls
	 * <i>this</i> method, will receive the {@link ClientUser} already built by the
	 * first. I.e., the second {@link #getBunchOUsers(GID...)} request's "updated"
	 * {@link UserValue} information is ignored and actually discarded. This is to
	 * ensure consistency between users of the cache; values in the cache that are
	 * already present should not be replaced so that there is never a case where
	 * two, distinct {@link ClientUser} objects are floating around. (Because if one
	 * changes due to an API invocation, e.g. unfriending it, the other will not
	 * know to update its state.)
	 * </p>
	 * <p>
	 * This method is designed to be called internally, by {@link ArlithClient} API
	 * classes and code. If called externally, it may break the state of the
	 * {@link ArlithClient} and all child objects.
	 * </p>
	 * 
	 * @param u The {@link UserValue} to get the user of.
	 * @return The (possibly new) {@link ClientUser}.
	 * @author Palanath
	 */
	// TODO Move second paragraph of method documentation to class documentation and
	// rephrase.
	public ClientUser cache(UserValue u) {
		ClientUser user;
		synchronized (users) {
			if ((user = getLoadedUser(u.id())) == null)
				users.put(u.id(), user = new ClientUser(u.id(), this, u.username(), u.status(), u.messageCount(),
						u.discriminant()));
		}
		return user;
	}

	/**
	 * <p>
	 * Checks the cache for a user with the specified ID and, if found, returns it.
	 * Otherwise, creates a new {@link ClientUser}, without properties, with the
	 * specified {@link GID}, and returns it.
	 * </p>
	 * <p>
	 * <b>This method is designed to be called by internal {@link ArlithClient} API
	 * classes</b> that receive {@link GID}s from the server. It should <b>never</b>
	 * be called with a {@link GID} that was not received from the server or does
	 * not represent a user, as it will create a new {@link ClientUser} object for
	 * the {@link GID} in such a case, whose server-querying methods will cause
	 * errors. This method should only be called to get or create new
	 * {@link ClientUser}s for {@link GID}s that are known to represent a user.
	 * </p>
	 * <p>
	 * Note that the resulting {@link ClientUser} will not have many of its
	 * properties assigned, including name, status, etc. Attempts to retrieve these
	 * properties will query them from the server.
	 * </p>
	 * 
	 * @param id The {@link GID} of the user to safely get an object of from the
	 *           cache.
	 * @return A (newly created), cached {@link ClientUser}.
	 */
	public ClientUser getUser(GID id) {
		ClientUser user;
		synchronized (users) {
			user = getLoadedUser(id);
			if (user == null)
				users.put(id, user = new ClientUser(id, this));
		}
		return user;
	}

	public List<ClientCommunity> listJoinedCommunities()
			throws ServerError, RestrictedError, RateLimitError, SyntaxError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(listJoinedCommunitiesRequest());
	}

	public CompletableFuture<List<ClientCommunity>> listJoinedCommunitiesRequest() {
		return joinedCommunities.futureUnmodifiable();
	}

	/**
	 * Returns an unmodifiable view of the list of friends that the logged in user
	 * has.
	 * 
	 * @return An unmodifiable view of the list of users who are friended with the
	 *         logged in user.
	 * @throws Error                                  If an {@link Error} occurs
	 *                                                during the request.
	 * @throws CommunicationProtocolConstructionError If reconstructing the server's
	 *                                                response into Java objects
	 *                                                fails.
	 * @throws IllegalCommunicationProtocolException  If, specifically, the server
	 *                                                responds with an exception
	 *                                                that it should not have for
	 *                                                this request.
	 * @throws SyntaxError                            If the server thinks request
	 *                                                being made is syntactically
	 *                                                incorrect. (This can usually
	 *                                                happen if the protocol changes
	 *                                                between versions, and the
	 *                                                server and client are running
	 *                                                those different versions.)
	 * @throws RateLimitError                         If the server is rate limiting
	 *                                                the client.
	 * @throws RestrictedError                        If the server denies
	 *                                                permission to the client to
	 *                                                run this request (this is
	 *                                                invoked for this request if
	 *                                                the user is not logged in, and
	 *                                                should not happen for the
	 *                                                client).
	 * @throws ServerError                            If an internal error that the
	 *                                                server does not expect occurs
	 *                                                on the server's end. This
	 *                                                means the server failed to run
	 *                                                the request; not that it
	 *                                                didn't want to.
	 * @throws CommunicationProtocolError             If the list has not yet been
	 *                                                requested from the server so
	 *                                                it is requested, and that
	 *                                                request fails with a
	 *                                                {@link CommunicationProtocolError}.
	 * @throws RuntimeException                       If the list has not yet been
	 *                                                requested from the server so
	 *                                                it is requested, and that
	 *                                                request fails with a
	 *                                                {@link RuntimeException}.
	 */
	public List<ClientUser> listFriends() throws ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(listFriendsRequest());
	}

	public CompletableFuture<List<ClientUser>> listFriendsRequest() {
		return friends.futureUnmodifiable();
	}

	void reconnect() {
		stop();
		startup();
	}

	/**
	 * <p>
	 * Registers an {@link EventHandler} that will be notified of the specified type
	 * of event.
	 * </p>
	 * 
	 * @param <T>     The type of the event to handle.
	 * @param type    The {@link EventType} object representing the type of the
	 *                event to handle.
	 * @param handler The {@link EventHandler} to register, that will receive the
	 *                events for processing.
	 * @author Palanath
	 */
	public <T extends CommunicationProtocolEvent> void register(EventType<T> type, EventHandler<? super T> handler) {
		eventManager.register(type, handler);
	}

	/**
	 * Called after successful login to set up the event handler and the
	 * auto-reconnect system.
	 */
	synchronized void startup() {
		if (running)
			throw new IllegalStateException("Application Client already running!");
		running = true;
		eventSubsystem.start();
		requestQueue.start();
	}

	public synchronized void stop() {
		running = false;
		// Enter synchronization on the client.

		// While we've stolen processing, we notify other threads that a shutdown has
		// caused their stop.
		eventSubsystem.stop();
		requestQueue.stop();
	}

	public <T extends CommunicationProtocolEvent> void unregister(EventType<T> type, EventHandler<? super T> handler) {
		eventManager.unregister(type, handler);
	}

}
