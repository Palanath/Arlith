package pala.apps.arlith.libraries.watchables;

public class View<V> extends DerivativeWatchable<V, V> {

	private final Watchable<? extends V> other;

	public View(Watchable<? extends V> other, boolean weak) {
		super(weak);
		this.other = other;
	}

	public static <V> View<V> view(Watchable<? extends V> watchable) {
		return new View<>(watchable, false);
	}

	public static <V> View<V> weakView(Watchable<? extends V> watchable) {
		return new View<>(watchable, true);
	}

	@Override
	public V getValue() {
		return other.getValue();
	}

	@Override
	protected void registerThis(Watcher<V> tw) {
		other.register(tw);
	}

	@Override
	protected void unregisterThis(Watcher<V> tw) {
		other.unregister(tw);
	}
}
