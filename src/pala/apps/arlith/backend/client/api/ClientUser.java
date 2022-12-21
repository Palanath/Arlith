package pala.apps.arlith.backend.client.api;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import pala.apps.arlith.application.Logging;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.Cache;
import pala.apps.arlith.backend.client.api.caching.ClientCache;
import pala.apps.arlith.backend.client.api.caching.WatchableCache;
import pala.apps.arlith.backend.client.api.caching.WatchableCache.Populator;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.events.LazyProfileIconChangedEvent;
import pala.apps.arlith.backend.common.protocol.events.StatusChangedEvent;
import pala.apps.arlith.backend.common.protocol.requests.GetProfileIconRequest;
import pala.apps.arlith.backend.common.protocol.requests.GetUserRequest;
import pala.apps.arlith.backend.common.protocol.requests.OpenDirectConversationRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.apps.arlith.libraries.streams.InputStream;
import pala.apps.arlith.libraries.watchables.Variable;
import pala.apps.arlith.libraries.watchables.View;

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

	protected final WatchableCache<String> username, status, discriminant;
	protected final WatchableCache<Long> messageCount;

	{
		Populator pop = new Populator() {

			@Override
			public void populate(CommunicationConnection connection)
					throws CommunicationProtocolError, RuntimeException {
				UserValue inq = new GetUserRequest(new GIDValue(id())).inquire(connection);
				username.populate(inq.username());
				status.populate(inq.status());
				messageCount.populate(inq.messageCount());
				discriminant.populate(inq.discriminant());
			}
		};
		username = new WatchableCache<>(client()::getRequestSubsystem, pop);
		status = new WatchableCache<>(client()::getRequestSubsystem, pop);
		messageCount = new WatchableCache<>(client()::getRequestSubsystem, pop);
		discriminant = new WatchableCache<>(client()::getRequestSubsystem, pop);
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
			Image i = getBaseProfileIcon();
			WritableImage img = new WritableImage((int) i.getWidth(), (int) i.getHeight());

			double namehash = 0;
			for (char c : identifier.toCharArray())
				namehash += c;
			namehash /= identifier.length();

			double hueshift = namehash * 360;
			for (int j = 0; j < img.getWidth(); j++)
				for (int j2 = 0; j2 < img.getHeight(); j2++) {
					Color c = i.getPixelReader().getColor(j, j2);
					img.getPixelWriter().setColor(j, j2, Color.hsb(hueshift, 1, c.getBrightness(), c.getOpacity()));
				}
			return img;
		}
	}

	protected final Cache<ClientThread> dmThread = new ClientCache.ClientCacheMaker<>(client()::getRequestSubsystem,
			a -> client().getThread(new OpenDirectConversationRequest(new GIDValue(id())).inquire(a).getGid()));
	protected final WatchableCache<Image> profileIcon = new WatchableCache<>(client()::getRequestSubsystem, a -> {
		GetProfileIconRequest req = new GetProfileIconRequest(new GIDValue(id()));
		req.sendRequest(a);
		PieceOMediaValue m = req.receiveResponse(a);
		// The media is optional; this user might not have a pfp (in which case we need
		// to calculate one).

		return getProfileImage(m, getIdentifier());
	});

	public View<String> usernameView() {
		return username.getView();
	}

	public View<String> statusView() {
		return status.getView();
	}

	public View<Long> messageCountView() {
		return messageCount.getView();
	}

	public View<String> discriminantView() {
		return discriminant.getView();
	}

	public View<Image> profileIconView() {
		return profileIcon.getView();
	}

	/**
	 * Boolean to denote whether this user has a profile icon set. This does not
	 * denote whether one the pfi of this user has been requested and obtained
	 * successfully. That information can be determined with the expression:
	 * <code>(profileIcon != null)</code>.
	 */
	private final Variable<Boolean> hasProfileIcon = new Variable<>(false);

	public View<Boolean> hasProfileIconView() {
		return hasProfileIcon.getView();
	}

	public ClientUser(GID gid, ArlithClient client) {
		super(gid, client);
	}

	public ClientUser(GID gid, ArlithClient client, String username, String status, long messageCount, String discriminant) {
		this(gid, client);
		this.username.populate(username);
		this.status.populate(status);
		this.messageCount.populate(messageCount);
		this.discriminant.populate(discriminant);
	}

	public ActionInterface<String> getNameRequest() {
		return username.get();
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

	public String getName() throws CommunicationProtocolError, RuntimeException {
		return getNameRequest().get();
	}

	public ActionInterface<String> getStatusRequest() {
		return status.get();
	}

	public String getStatus() throws CommunicationProtocolError, RuntimeException {
		return getStatusRequest().get();
	}

	public ActionInterface<Long> getMessageCountRequest() {
		return messageCount.get();
	}

	public long getMessageCount() throws CommunicationProtocolError, RuntimeException {
		return getMessageCountRequest().get();
	}

	public ActionInterface<String> getDiscriminantRequest() {
		return discriminant.get();
	}

	public String getDiscriminant() throws CommunicationProtocolError, RuntimeException {
		return getDiscriminantRequest().get();
	}

	public ActionInterface<Void> friendRequest() {
		return client().friendRequest(id());
	}

	public void friend() throws CommunicationProtocolError, RuntimeException {
		client().friend(id());
	}

	public ActionInterface<ClientThread> openDirectConversationRequest() {
		return dmThread.get();
	}

	public ClientThread openDirectConversation() throws CommunicationProtocolError, RuntimeException {
		return openDirectConversationRequest().get();
	}

	public void receiveStatusChangeEvent(StatusChangedEvent event) {
		status.update(event.getNewStatus().getValue());
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
		if (profileIcon != null)
			try {
				PieceOMediaValue m = client().getRequestSubsystem()
						.action(new GetProfileIconRequest(new GIDValue(id()))).get();
				profileIcon.update(getProfileImage(m, getIdentifier()));
			} catch (RuntimeException | CommunicationProtocolError e) {
				B: {
					try {
						Logging.err("Failed to obtain the changed profile icon of the user " + username.get() == null
								? String.valueOf(id())
								: username.get().get());
					} catch (CommunicationProtocolError | RuntimeException e1) {
						e1.addSuppressed(e);
						Logging.err(e1);
						break B;
					}
					Logging.err(e);
				}
			}
	}

	public Image getProfileIcon() throws CommunicationProtocolError, RuntimeException {
		return getProfileIconRequest().get();
	}

	public boolean hasProfileIcon() throws CommunicationProtocolError, RuntimeException {
		if (profileIcon.get() == null)
			getProfileIcon();
		return hasProfileIcon.getValue();
	}

	public ActionInterface<Image> getProfileIconRequest() {
		return profileIcon.get();
	}

}
