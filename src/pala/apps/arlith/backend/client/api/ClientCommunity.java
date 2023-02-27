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
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.MediaNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
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
import pala.apps.arlith.libraries.watchables.Watchable;

public class ClientCommunity extends SimpleClientObject implements Named {
	public ClientCommunity(GID gid, ArlithClient client, String name, List<ClientThread> threads, List<GID> members) {
		super(gid, client);
		this.name = new WatchableCache<>(name);
		this.threads = threads;
		memberIDs = members;
	}

	private static WritableImage autogenerateIcon(GID id) {
		WritableImage img = new WritableImage(1, 1);

		double namehash = 0;
		for (char c : id.getHex().toCharArray())
			namehash += c;
		namehash /= id.getHex().length();
		img.getPixelWriter().setColor(0, 0, Color.hsb(namehash * 360, 1, 1));

		return img;
	}

	private static WritableImage autogenerateBackgroundImage(GID id) {
		return new WritableImage(1, 1);
	}

	private final Function<PieceOMediaValue, Image> ICON_REQUEST_RESULT_TO_IMAGE = a -> a != null
			? new Image(new ByteArrayInputStream(a.getMedia()))
			: autogenerateIcon(id()),
			BACKGROUND_IMAGE_REQUEST_RESULT_TO_IMAGE = a -> a != null
					? new Image(new ByteArrayInputStream(a.getMedia()))
					: autogenerateBackgroundImage(id());

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
			() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon")), ICON_REQUEST_RESULT_TO_IMAGE,
			client().getRequestQueue()),
			backgroundImage = new WatchableCache<>(
					() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")),
					BACKGROUND_IMAGE_REQUEST_RESULT_TO_IMAGE, client().getRequestQueue());

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
		return icon.expression(a -> !(a instanceof WritableImage));
	}

	public Watchable<Image> backgroundImageView() {
		return backgroundImage.getView();
	}

	public Watchable<Boolean> hasBackgroundImageView() {
		return backgroundImage.expression(a -> !(a instanceof WritableImage));
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
		return !(getIcon() instanceof WritableImage);
	}

	public Image getBackgroundImage()
			throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return getValueWithDefaultExceptions(getBackgroundImageRequest(), MediaNotFoundError.class);
	}

	public boolean hasBackgroundImage()
			throws MediaNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
		return !(getBackgroundImage() instanceof WritableImage);
	}

	public void receiveLazyImageChangedEvent(LazyCommunityImageChangedEvent event) {
		switch (event.getType().getValue()) {
		case "icon":
			if (icon.getValue() != null)
				// Force request pfp. (Ignore cache.)
				client().getRequestQueue()
						.queueFuture(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon")))
						.thenApply(ICON_REQUEST_RESULT_TO_IMAGE).thenAccept(icon::updateItem).exceptionally(b -> {
							client().getLogger().err(
									"Failed to obtain the changed icon of the Community: " + name.getValue() == null
											? String.valueOf(id())
											: name.getValue());
							client().getLogger().err(b);
							return null;
						});
			break;
		case "background-image":
			if (icon != null)
				// Force request pfp. (Ignore cache.)
				client().getRequestQueue()
						.queueFuture(
								new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")))
						.thenApply(BACKGROUND_IMAGE_REQUEST_RESULT_TO_IMAGE).thenAccept(backgroundImage::updateItem)
						.exceptionally(a -> {
							client().getLogger().err(
									"Failed to obtain the changed icon of the Community: " + name.getValue() == null
											? String.valueOf(id())
											: name.getValue());
							client().getLogger().err(a);
							return null;
						});
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
	 * @throws ObjectNotFoundError                    If any of the member IDs are
	 *                                                not found. (This should never
	 *                                                happen.)
	 * @throws AccessDeniedError                      If any of the member IDs are
	 *                                                deemed not accessible by this
	 *                                                account on the server.
	 */
	public Set<ClientUser> getMembers()
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
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
	 * @throws ObjectNotFoundError                    If any of the member IDs are
	 *                                                not found. (This should never
	 *                                                happen.)
	 * @throws AccessDeniedError                      If any of the member IDs are
	 *                                                deemed not accessible by this
	 *                                                account on the server.
	 */
	public Set<ClientUser> getMembers(int from, int to)
			throws AccessDeniedError, ObjectNotFoundError, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError, RuntimeException, Error {
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
