package pala.apps.arlith.libraries.watchables;

import java.lang.ref.WeakReference;

public class WeakWatcher<V> implements Watcher<V> {

	private final WeakReference<Watcher<V>> watcher;

	public Watcher<V> getWatcher() {
		return watcher.get();
	}

	public WeakWatcher(Watcher<V> watcher) {
		this.watcher = new WeakReference<>(watcher);
	}

	public static <V> WeakWatcher<V> weak(Watcher<V> w) {
		return w instanceof WeakWatcher ? (WeakWatcher<V>) w : new WeakWatcher<>(w);
	}

	@Override
	public void watch(V previousValue, V newValue, Watchable<? extends V> watchable) {
		Watcher<V> w = watcher.get();
		if (w == null)
			watchable.unregister(this);
		else
			w.watch(previousValue, newValue, watchable);
	}

	@Override
	public void watch(V previousValue, V newValue) {
		throw new UnsupportedOperationException();
	}

}
