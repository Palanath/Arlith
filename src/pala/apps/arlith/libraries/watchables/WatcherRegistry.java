package pala.apps.arlith.libraries.watchables;

import java.util.ArrayList;
import java.util.List;

public abstract class WatcherRegistry<V> implements Watchable<V> {

	protected List<Watcher<? super V>> watchers = new ArrayList<>(5);

	@Override
	public synchronized void register(Watcher<? super V> watcher) {
		if (watcher == null)
			throw null;
		watchers.add(watcher);
	}

	@Override
	public synchronized void unregister(Watcher<? super V> watcher) {
		if (watcher == null)
			return;
		watchers.remove(watcher);
	}

	protected synchronized void notifyWatchers(V oldValue, V newValue) {
		for (Watcher<? super V> w : watchers)
			w.watch(oldValue, newValue, this);
	}

}