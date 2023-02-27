package pala.apps.arlith.backend.client.api;

import static pala.apps.arlith.libraries.CompletableFutureUtils.getValueWithDefaultExceptions;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.v2.WatchableCache;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.MediaNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.LazyCommunityImageChangedEvent;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.GetCommunityImageRequest;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.PieceOMediaValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.watchables.CompositeWatchable;
import pala.apps.arlith.libraries.watchables.Variable;
import pala.apps.arlith.libraries.watchables.View;
import pala.apps.arlith.libraries.watchables.Watchable;

public class ClientCommunity extends SimpleClientObject implements Named {
	public ClientCommunity(GID gid, ArlithClient client, String name, List<ClientThread> threads, List<GID> members) {
		super(gid, client);
		this.name = new WatchableCache<>(name);
		this.threads = threads;
		memberIDs = members;
	}

	private static final Function<PieceOMediaValue, Image> NULLABLE_PIECEOMEDIA_TO_IMAGE_FUNCTION = a -> a == null
			? null
			: new Image(new ByteArrayInputStream(a.getMedia()));

	/*
	 * If unpopulated, these will contain null.
	 * 
	 * Otherwise, (�) if the community has images, they will contain the images, and
	 * (�) if the community does not contain a respective image, the cache for that
	 * image will contain an auto-generated image.
	 * 
	 * The autogenerated image for the icon is (currently) a single pixel at full
	 * brightness and saturation, whose hue is based off of a "hash" of the
	 * community's ID. TODO This needs to be updated to use something more iconic,
	 * like the Arlith feather that profile pictures use.
	 * 
	 * The autogenerated image for the bacckground is a single transparent pixel.
	 */
	protected final WatchableCache<Image> icon = new WatchableCache<>(
			() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon")), a -> {
				if (a != null)
					return new Image(new ByteArrayInputStream(a.getMedia()));
				else {
					WritableImage img = new WritableImage(1, 1);

					double namehash = 0;
					for (char c : idHex().toCharArray())
						namehash += c;
					namehash /= getName().length();
					img.getPixelWriter().setColor(0, 0, Color.hsb(namehash * 360, 1, 1));

					return img;
				}
			}, client().getRequestQueue()), backgroundImage = new WatchableCache<>(
					() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")), a -> {
						if (a != null)
							return new Image(new ByteArrayInputStream(a.getMedia()));
						else
							return new WritableImage(1, 1);
					}, client().getRequestQueue());

	/**
	 * <p>
	 * Returns a view of the cache of this {@link ClientCommunity}'s {@link #icon}.
	 * Note that attempting to obtain the image from this view will <b>not</b> query
	 * the image from the server. To query the image, {@link #getIcon()} or
	 * {@link #getIconRequest()} should be used. This view will automatically be
	 * updated appropriately.
	 * </p>
	 * 
	 * @return
	 */
	public Watchable<Image> iconView() {
		return icon.getView();
	}

	public Watchable<Boolean> hasIconView() {
		// Poorman's check for presence; check if image was autogenerated or not.
		return icon.expression(a -> a instanceof WritableImage);
	}

	public Watchable<Image> backgroundImageView() {
		return backgroundImage.getView();
	}

	public Watchable<Boolean> hasBackgroundImageView() {
		return backgroundImage.expression(a -> a instanceof WritableImage);
	}

	public CompletableFuture<Image> getIconRequest() {
		return icon.future();
	}

	public CompletableFuture<Image> getBackgroundImageRequest() {
		return backgroundImage.future();
	}

	public Image getIcon() throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getIconRequest(), MediaNotFoundError.class);
	}

	public boolean hasIcon() throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		if (icon.getValue() == null)
			getIcon();
		return hasIcon.getValue();
	}

	public Image getBackgroundImage()
			throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getBackgroundImageRequest(), MediaNotFoundError.class);
	}

	public boolean hasBackgroundImage()
			throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		if (backgroundImage.getValue() == null)
			getBackgroundImage();
		return hasBackgroundImage.getValue();
	}

	public void receiveLazyImageChangedEvent(LazyCommunityImageChangedEvent event) {
		switch (event.getType().getValue()) {
		case "icon":
			if (icon.getValue() != null)
				try {
					// Force request pfp. (Ignore cache.)
					client().getRequestQueue()
							.queueFuture(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon")))
							.thenAccept(a -> {

							});
					client().getRequestSubsystem()
							.action(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon"))).then(a -> {
								icon.set(new Image(new ByteArrayInputStream(a.getMedia())));
								return icon.getValue();
							});
				} catch (CommunicationProtocolConstructionError e) {
					client().getLogger()
							.err("Failed to obtain the changed icon of the Community: " + name.getValue() == null
									? String.valueOf(id())
									: name.getValue());
				}
			break;
		case "background-image":
			if (icon != null)
				try {
					// Force request pfp. (Ignore cache.)
					client().getRequestSubsystem()
							.action(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")))
							.then(a -> {
								icon.set(new Image(new ByteArrayInputStream(a.getMedia())));
								return icon.getValue();
							});
				} catch (CommunicationProtocolConstructionError e) {
					client().getLogger()
							.err("Failed to obtain the changed icon of the Community: " + name.getValue() == null
									? String.valueOf(id())
									: name.getValue());
				}
			break;
		default:
			throw new RuntimeException(
					"The server returned invalid information for which community image has changed. (The server did not specify \"icon\" for the community icon or \"background-image\" for the community's background image.)");
		}
	}

	private final WatchableCache<String> name;
	private final List<ClientThread> threads;
	private final List<GID> memberIDs;

	public Watchable<String> nameView() {
		return name.getView();
	}

	public int getMemberCount() {
		return memberIDs.size();
	}

	public String getName() {
		return name.getValue();
	}

	public List<ClientThread> getThreads() {
		return threads;
	}

	public List<GID> getMemberIDs() {
		return memberIDs;
	}

	// TODO Add a getUsersInBulk method or smth to the Cli and then use it in the
	// following two methods.

	/**
	 * Returns a list of {@link ClientUser} objects corresponding to the users
	 * represented by the {@link GID}s in {@link #getMemberIDs()}. The users are in
	 * the same order. Please note that this list is rebuilt upon each call to this
	 * method. It is typically faster to simply query {@link #getMemberIDs()} and
	 * call any user-reifying methods on the client object, such as
	 * {@link ArlithClient#getUser(GID)}.
	 * 
	 * @return A brand new {@link Set} that was populated by converting each value
	 *         in {@link #getMemberIDs()} to a {@link ClientUser} using
	 *         {@link #client()}.{@link ArlithClient#cache(pala.apps.arlith.backend.common.protocol.types.UserValue)}.
	 * @throws CommunicationProtocolError If a {@link CommunicationProtocolError}
	 *                                    occurs during the querying of any
	 *                                    {@link UserValue}s.
	 * @throws RuntimeException           If a {@link RuntimeException} occurs
	 *                                    during the querying of any
	 *                                    {@link UserValue}s.
	 */
	public Set<ClientUser> getMembers() throws CommunicationProtocolError, RuntimeException {
		return client().getBunchOUsers(memberIDs);
	}

	/**
	 * Returns a list of {@link ClientUser} objects (reified users; like
	 * {@link #getMembers()}), but within the specified range. This method is
	 * beneficial in contrast to {@link #getMembers()} in that it only queries the
	 * users in the specified range. Please note that the member list can constantly
	 * change with users joining and leaving communities.
	 * 
	 * @param from The beginning index, inclusive, of the members to retrieve
	 *             {@link ClientUser} representations of.
	 * @param to   The ending index, exclusive, of the members to retrieve.
	 * @return The resulting {@link List} containing the retrieved
	 *         {@link ClientUser} objects.
	 * @throws CommunicationProtocolError If a {@link CommunicationProtocolError}
	 *                                    occurs during the querying of any
	 *                                    {@link UserValue}s.
	 * @throws RuntimeException           If a {@link RuntimeException} occurs
	 *                                    during querying of any {@link UserValue}s.
	 */
	public Set<ClientUser> getMembers(int from, int to) throws CommunicationProtocolError, RuntimeException {
		if (to > getMemberCount())
			to = getMemberCount();
		if (from < 0)
			from = 0;
		return client().getBunchOUsers(memberIDs.subList(from, to));
	}

	@Override
	public String name() {
		return name.getValue();
	}

}
