package pala.apps.arlith.backend.watchables;

import java.util.ArrayList;
import java.util.List;

public abstract class WatcherRegistry<V> implements Watchable<V> {

	protected List<Watcher<? super V>> watchers = new ArrayList<>(5);
	private List<Op> queue = new ArrayList<>();
	private volatile boolean iterating;

	private class Op {
		private final boolean add;
		private final Watcher<? super V> watcher;

		{
			queue.add(this);
		}

		private void op() {
			if (add)
				watchers.add(watcher);
			else
				watchers.remove(watcher);
		}

		private Op(boolean add, Watcher<? super V> watcher) {
			this.add = add;
			this.watcher = watcher;
		}

	}

	@Override
	public void register(Watcher<? super V> watcher) {
		if (watcher == null)
			throw null;
		if (iterating)
			new Op(true, watcher);
		else
			watchers.add(watcher);
	}

	@Override
	public void unregister(Watcher<? super V> watcher) {
		if (watcher == null)
			return;
		if (iterating)
			new Op(false, watcher);
		else
			watchers.remove(watcher);
	}

	protected synchronized void notifyWatchers(V oldValue, V newValue) {
		iterating = true;
		for (Watcher<? super V> w : watchers)
			w.watch(oldValue, newValue, this);
		iterating = false;
		for (Op o : queue)
			o.op();
		queue.clear();
	}

}