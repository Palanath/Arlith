package pala.apps.arlith.backend.client.api;

import static pala.apps.arlith.libraries.CompletableFutureUtils.getValueWithDefaultExceptions;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.client.api.caching.v2.WatchableCache;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.MediaNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.StatusChangedEvent;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.GetProfileIconRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetUserRequest;
import pala.apps.arlith.backend.common.protocol.requests.OpenDirectConversationRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.streams.InputStream;
import pala.apps.arlith.libraries.watchables.Watchable;

public class ClientUser extends SimpleClientObject implements Named {

	private static WeakReference<Image> BASE_PROFILE_ICON;

	/**
	 * <p>
	 * Returns the official default profile icon. This function loads the icon
	 * lazily, so, if the icon has not been loaded yet, it loads it and caches it.
	 * Additionally, the icon is cached in a {@link WeakReference}, so if the Java
	 * garbage collector collects the icon from the cache, it will become unloaded
	 * and the next invocation of this method thereafter will load it again. The
	 * icon itself is bundled into the application and is loaded from the location
	 * <code>"/pala/apps/arlith/logo.png"</code> inside the app's classpath.
	 * </p>
	 * <p>
	 * More specifically, this function checks if {@link #BASE_PROFILE_ICON} is
	 * <code>null</code>, or if it is non-<code>null</code> but the {@link Image} it
	 * points to is <code>null</code>. If either is the case, this function creates
	 * a new {@link WeakReference} pointing to a new {@link Image} constructed by
	 * calling {@link Image#Image(java.io.InputStream)} with an {@link InputStream}
	 * obtained from this class's {@link Class#getResourceAsStream(String)} method,
	 * called with the absolute path of the default profile icon
	 * (<code>/pala/apps/arlith/logo.png</code>). The newly loaded image is then
	 * returned. If neither object were <code>null</code>, then the already-loaded
	 * image is simply returned.
	 * </p>
	 * 
	 * @return The official default profile icon.
	 */
	public static Image getBaseProfileIcon() {
		Image i;
		if (BASE_PROFILE_ICON == null || (i = BASE_PROFILE_ICON.get()) == null)
			BASE_PROFILE_ICON = new WeakReference<>(
					i = new Image(ClientUser.class.getResourceAsStream("/pala/apps/arlith/logo.png")));
		return i;
	}

	public static Image getBaseProfileIcon(double hueShift) {
		Image i = getBaseProfileIcon();
		WritableImage img = new WritableImage((int) i.getWidth(), (int) i.getHeight());
		for (int j = 0; j < img.getWidth(); j++)
			for (int j2 = 0; j2 < img.getHeight(); j2++) {
				Color c = i.getPixelReader().getColor(j, j2);
				img.getPixelWriter().setColor(j, j2, Color.hsb(hueShift, 1, c.getBrightness(), c.getOpacity()));
			}
		return img;
	}

	protected final WatchableCache<String> username, status, discriminant;
	protected final WatchableCache<Long> messageCount;

	{
		Function<UserValue, UserValue> update = uv -> {
			((ClientUser) this).username.updateItem(uv.username());
			((ClientUser) this).status.updateItem(uv.status());
			((ClientUser) this).messageCount.updateItem(uv.messageCount());
			((ClientUser) this).discriminant.updateItem(uv.discriminant());
			return uv;
		};
		GetUserRequest req = new GetUserRequest(new GIDValue(id()));

		username = new WatchableCache<>(req, update.andThen(UserValue::username), client().getRequestQueue());
		status = new WatchableCache<>(req, update.andThen(UserValue::status), client().getRequestQueue());
		messageCount = new WatchableCache<>(req, update.andThen(UserValue::messageCount), client().getRequestQueue());
		discriminant = new WatchableCache<>(req, update.andThen(UserValue::discriminant), client().getRequestQueue());
	}

	/**
	 * <p>
	 * This function gets the {@link Image} contained in the {@link PieceOMediaValue
	 * media} provided, if any, or calculates this user's PFP based off of the
	 * provided {@link String} identifier. This function is used as convenience to
	 * return a generated PFP if a user does not have one.
	 * </p>
	 * <p>
	 * More specifically, this function checks if the provided
	 * {@link PieceOMediaValue} is <code>null</code>. If it is
	 * Not-<code>null</code>, this function returns a new {@link Image} created from
	 * the byte data of the provided {@link PieceOMediaValue}. If the media is
	 * <code>null</code>, this function creates a new {@link WritableImage} based
	 * off of the {@link #BASE_PROFILE_ICON}, (see {@link #getBaseProfileIcon()} for
	 * more details). Specifically, this function hashes the identifier provided and
	 * picks a Hue. It then copies the default profile icon, shifting every pixel's
	 * hue by the Hue it calculated from the hash.
	 * </p>
	 * 
	 * @param m          The {@link PieceOMediaValue} to get the image from. If this
	 *                   argument (itself) is <code>null</code>, this function will
	 *                   calculate a pfp.
	 * @param identifier The identifier, only used when calculating the pfp of the
	 *                   user if the first argument is <code>null</code>.
	 * @return An {@link Image} of this user's PFP. This function is considered the
	 *         standard for what a user's profile icon should look like in a
	 *         Application client.
	 */
	private static Image getProfileImage(PieceOMediaValue m, String identifier) {
		if (m != null)
			return new Image(new ByteArrayInputStream(m.getMedia()));
		else {
			double namehash = 0;
			for (char c : identifier.toCharArray())
				namehash += c;

			return getBaseProfileIcon(namehash / identifier.length() * 360);
		}
	}

	protected final NewCache<ClientThread> dmThread = new NewCache<>(
			new OpenDirectConversationRequest(new GIDValue(id())), a -> client().getThread(a.getGid()),
			client().getRequestQueue());

	/**
	 * {@link WatchableCache} of the user's profile icon. Contains <code>null</code>
	 * until queried. Upon successful population, the cache will contain either an
	 * {@link Image} returned from the server or a newly made {@link WritableImage}
	 * made from {@link #getProfileImage(PieceOMediaValue, String)} with this
	 * {@link ClientUser}'s {@link #idHex()} as an identifier.
	 */
	protected final WatchableCache<Image> profileIcon = new WatchableCache<>(
			new GetProfileIconRequest(new GIDValue(id())), a -> getProfileImage(a, idHex()),
			client().getRequestQueue());

	public Watchable<String> usernameView() {
		return username.getView();
	}

	public Watchable<String> statusView() {
		return status.getView();
	}

	public Watchable<Long> messageCountView() {
		return messageCount.getView();
	}

	public Watchable<String> discriminantView() {
		return discriminant.getView();
	}

	public Watchable<Image> profileIconView() {
		return profileIcon.getView();
	}

	public Watchable<Boolean> hasProfileIconView() {
		return profileIcon.expression(a -> !(a instanceof WritableImage));
	}

	public ClientUser(GID gid, ArlithClient client) {
		super(gid, client);
	}

	public ClientUser(GID gid, ArlithClient client, String username, String status, long messageCount,
			String discriminant) {
		this(gid, client);
		this.username.updateItem(username);
		this.status.updateItem(status);
		this.messageCount.updateItem(messageCount);
		this.discriminant.updateItem(discriminant);
	}

	public CompletableFuture<String> getNameRequest() {
		return username.future();
	}

	/**
	 * The same as {@link #getName()} but swallows errors with a
	 * {@link RuntimeException}.
	 */
	@Override
	public String name() throws RuntimeException {
		try {
			return getName();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getIdentifier() throws CommunicationProtocolError, RuntimeException {
		String name = getName() + '#';
		if (getDiscriminant().length() < 3)
			name += getDiscriminant().length() == 1 ? "00" : "0";
		return name + getDiscriminant();
	}

	public String getUsername()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getName();
	}

	public CompletableFuture<String> getUsernameRequest() {
		return getNameRequest();
	}

	public String getName()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getNameRequest(), AccessDeniedError.class, ObjectNotFoundError.class);
	}

	public CompletableFuture<String> getStatusRequest() {
		return status.future();
	}

	public String getStatus()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getStatusRequest(), AccessDeniedError.class, ObjectNotFoundError.class);
	}

	public CompletableFuture<Long> getMessageCountRequest() {
		return messageCount.future();
	}

	public long getMessageCount()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getMessageCountRequest(), AccessDeniedError.class,
				ObjectNotFoundError.class);
	}

	public CompletableFuture<String> getDiscriminantRequest() {
		return discriminant.future();
	}

	public String getDiscriminant()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getDiscriminantRequest(), AccessDeniedError.class,
				ObjectNotFoundError.class);
	}

	public CompletableFuture<Void> friendRequest() {
		return client().friendRequest(id());
	}

	public void friend() throws ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		client().friend(id());
	}

	public CompletableFuture<ClientThread> openDirectConversationRequest() {
		return dmThread.future();
	}

	public ClientThread openDirectConversation()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(openDirectConversationRequest(), AccessDeniedError.class,
				ObjectNotFoundError.class);
	}

	public void receiveStatusChangeEvent(StatusChangedEvent event) {
		status.updateItem(event.getNewStatus().getValue());
	}

	public void receiveLazyProfileIconChangeEvent(LazyProfileIconChangedEvent event) {
		refreshProfileIcon();
	}

	/**
	 * <p>
	 * Called to refresh the profile icon of this {@link ClientUser}. This method
	 * queries the profile icon of this user from the server and updates the
	 * {@link #profileIcon} property to reflect the value of whatever the server
	 * returns. This method does nothing if {@link #profileIcon} is
	 * <code>null</code>.
	 * </p>
	 * <p>
	 * This method is called by whenever the current profile picture is "dirty,"
	 * i.e., not up to date; so, it needs to be refreshed. For example, the server
	 * will send a "Lazy PFI Change Event," when this user's profile icon changes
	 * (meaning the profile icon the client currently has is wrong now, and the
	 * client needs to go and get the latest copy from the server), but the event
	 * itself <i>does not contain the new profile icon</i>, so the client must go
	 * and query the new profile icon, (if it needs it), once the icon changes. This
	 * method is also called by {@link ClientOwnUser#setProfileIcon(byte[])} if the
	 * server returns a successful response. (This is because the server may modify
	 * the profile icon before sending it back, and so, for consistency between what
	 * this client and other clients see, the client requests it back from the
	 * server.)
	 * </p>
	 */
	protected final void refreshProfileIcon() {
		if (profileIcon.isPopulated()) {
			client().getRequestQueue().queueFuture(new GetProfileIconRequest(new GIDValue(id())))
					.thenAccept(a -> profileIcon.updateItem(getProfileImage(a, idHex()))).exceptionally(a -> {
						try {
							client().getLogger().err(
									"Failed to obtain the changed profile icon of the user " + username.get() == null
											? String.valueOf(id())
											: getName());
						} catch (CommunicationProtocolError | RuntimeException e) {
							e.addSuppressed(a);
							client().getLogger().err("Failed to obtain the changed profile icon of user with ID: "
									+ idHex() + ". Also failed to obtain their username.");
							client().getLogger().err(e);
						}
						return null;
					});
		}
	}

	public Image getProfileIcon() throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getProfileIconRequest(), MediaNotFoundError.class);
	}

	public boolean hasProfileIcon()
			throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return !(getProfileIcon() instanceof WritableImage);
	}

	public CompletableFuture<Image> getProfileIconRequest() {
		return profileIcon.future();
	}

}
