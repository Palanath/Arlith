package pala.apps.arlith.libraries.watchables;

import java.util.function.BiFunction;

public class FusionWatchable<F, S, R> extends WatcherRegistry<R> {
	private final Watchable<F> first;
	private final Watchable<S> second;
	private final BiFunction<F, S, R> fuse;

	public FusionWatchable(Watchable<F> first, Watchable<S> second, BiFunction<F, S, R> fuse) {
		this.first = first;
		this.second = second;
		this.fuse = fuse;
		sw = (previousValue, newValue) -> notifyWatchers(fuse.apply(first.getValue(), previousValue),
				fuse.apply(first.getValue(), newValue));
		fw = (previousValue, newValue) -> notifyWatchers(fuse.apply(previousValue, second.getValue()),
				fuse.apply(newValue, second.getValue()));
	}

	private final Watcher<F> fw;
	private final Watcher<S> sw;

	@Override
	public void register(Watcher<? super R> watcher) {
		if (watchers.isEmpty()) {
			first.register(fw);
			second.register(sw);
		}
		super.register(watcher);
	}

	@Override
	public void unregister(Watcher<? super R> watcher) {
		super.unregister(watcher);
		if (watchers.isEmpty()) {
			first.unregister(fw);
			second.unregister(sw);
		}
	}

	@Override
	public R getValue() {
		return fuse.apply(first.getValue(), second.getValue());
	}

}
