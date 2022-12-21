package pala.apps.arlith.libraries.watchables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CompositeWatchable<R> extends WatcherRegistry<R> {

	private final Watcher<Object> tw = (previousValue, newValue) -> recalc();

	private final List<Source<?>> sources = new ArrayList<>();
	private final Supplier<R> valueCalculator;
	private R previous;

	public CompositeWatchable(Supplier<R> valueCalculator, Watchable<?>... watchables) {
		previous = (this.valueCalculator = valueCalculator).get();
		for (Watchable<?> w : watchables)
			new Source<>(w);
	}

	public List<Source<?>> getSourceView() {
		return Collections.unmodifiableList(sources);
	}

	public class Source<T> {

		public Watchable<T> getSource() {
			return source;
		}

		private final Watchable<T> source;

		private void register() {
			source.register(tw);
		}

		private void unregister() {
			source.unregister(tw);
		}

		public void remove() {
			sources.remove(this);
			unregister();
		}

		public Source(Watchable<T> source) {
			this.source = source;
			sources.add(this);
			if (!watchers.isEmpty())
				register();
		}
	}

	@Override
	public void register(Watcher<? super R> watcher) {
		if (watchers.isEmpty())
			for (Source<?> s : sources)
				s.register();
		super.register(watcher);
	}

	@Override
	public void unregister(Watcher<? super R> watcher) {
		super.unregister(watcher);
		if (watchers.isEmpty())
			for (Source<?> s : sources)
				s.unregister();
	}

	private void recalc() {
		notifyWatchers(previous, previous = valueCalculator.get());
	}

	@Override
	public R getValue() {
		return previous;
	}

}
