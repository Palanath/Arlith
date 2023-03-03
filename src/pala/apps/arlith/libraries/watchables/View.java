package pala.apps.arlith.libraries.watchables;

import pala.apps.arlith.backend.client.api.ClientCommunity;

/**
 * <p>
 * A {@link View} of another {@link Watchable} that does not permit setting.
 * This type is used primarily for instances of
 * <code>({@link Watchable} &amp; {@link Settable}</code> held by an API class
 * that wants to expose a "view" of the object without allowing callers to
 * change the value of the object.
 * </p>
 * <p>
 * For example, a {@link ClientCommunity}'s {@link ClientCommunity#getIcon()
 * icon} is held by a {@link Property}, which is set to by the
 * {@link ClientCommunity} class, but {@link ClientCommunity} needs to expose a
 * {@link Watchable} of this property to calling code, without allowing client
 * code to change the value in the property (the property should only be updated
 * by {@link ClientCommunity}). To do this, instead of having a method that
 * exposes the {@link Property} instance held by it holds, the
 * {@link ClientCommunity} class has a method that returns a {@link View} of the
 * {@link Property}. This allows it to provide client code with a
 * {@link Watchable} instance that does not permit changing the instance's
 * value, while still providing notification ({@link Watcher}) facilities to the
 * client.
 * </p>
 * <p>
 * Conceptually, a {@link View} is like a one-way visual display; one may see
 * the content it displays, but cannot change the such content.
 * </p>
 * 
 * @author Palanath
 *
 * @param <V> The type of value exposed by the {@link View}.
 */
public class View<V> extends Mask<V, V> {

	public View(Watchable<? extends V> other, boolean weak) {
		super(a -> a, other, weak);
	}

	public static <V> View<V> view(Watchable<? extends V> watchable) {
		return new View<>(watchable, false);
	}

	public static <V> View<V> weakView(Watchable<? extends V> watchable) {
		return new View<>(watchable, true);
	}

}
