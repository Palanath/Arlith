package pala.apps.arlith.backend.client.api;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.v2.WatchableCache;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
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
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.watchables.Variable;
import pala.apps.arlith.libraries.watchables.View;

import static pala.apps.arlith.libraries.CompletableFutureUtils.*;

public class ClientCommunity extends SimpleClientObject implements Named {
	public ClientCommunity(GID gid, ArlithClient client, String name, List<ClientThread> threads, List<GID> members) {
		super(gid, client);
		this.name.set(name);
		this.threads = threads;
		memberIDs = members;
	}


	// These need to be rethought.
	protected final WatchableCache<Image> icon = new WatchableCache<>(
			() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon")), a -> {
				hasIcon.set(a != null);
				return null;
			},
			client().getRequestQueue()),
			backgroundImage = new WatchableCache<>(
					() -> new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")),
					a -> null, client().getRequestQueue());

	public View<Image> iconView() {
		return icon.getView();
	}

	public View<Boolean> hasIconView() {
		return hasIcon.getView();
	}

	public View<Image> backgroundImageView() {
		return backgroundImage.getView();
	}

	public View<Boolean> hasBackgroundImageView() {
		return hasBackgroundImage.getView();
	}

	public CompletableFuture<Image> getIconRequest() {
		return client().getRequestQueue()
				.queueFuture(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("icon"))).thenApply(a -> {
					hasIcon.set(a != null);
					if (a == null) {
						// TODO Base default image off of community name (using hash) in the same way
						// that PFPs are derived.
						WritableImage img = new WritableImage(1, 1);
						icon.set(img);

						double namehash = 0;
						for (char c : getName().toCharArray())
							namehash += c;
						namehash /= getName().length();
						img.getPixelWriter().setColor(0, 0, Color.hsb(namehash * 360, 1, 1));
					} else
						icon.set(new Image(new ByteArrayInputStream(a.getMedia())));
					return icon.getValue();
				});
	}

	public CompletableFuture<Image> getBackgroundImageRequest() {
		return client().getRequestQueue()
				.queueFuture(new GetCommunityImageRequest(new GIDValue(id()), new TextValue("background-image")))
				.thenApply(a -> {
					hasBackgroundImage.set(a != null);
					if (a != null)
						backgroundImage.set(new Image(new ByteArrayInputStream(a.getMedia())));
					else
						// Set to default value.
						backgroundImage.set(null);
					return backgroundImage.getValue();
				});
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

	/**
	 * The name of this community. This should always be supplied with whatever
	 * package of data this object spawned from that was sent with the server.
	 */
	private final Variable<String> name = new Variable<>();
	private final List<ClientThread> threads;
	private final List<GID> memberIDs;

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
