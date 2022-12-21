package pala.apps.arlith.backend.watchables;

public abstract class MultiDerivativeWatchable<S, R> extends DerivativeWatchable<S, R> {

	protected final Watchable<? extends S>[] sources;

	@SafeVarargs
	public MultiDerivativeWatchable(boolean weak, Watchable<? extends S>... sources) {
		super(weak);
		this.sources = sources;
	}

	@SafeVarargs
	public MultiDerivativeWatchable(Watchable<? extends S>... sources) {
		this(false, sources);
	}

	@Override
	protected void registerThis(Watcher<S> tw) {
		for (Watchable<? extends S> w : sources)
			w.register(tw);
	}

	@Override
	protected void unregisterThis(Watcher<S> tw) {
		for (Watchable<? extends S> w : sources)
			w.unregister(tw);
	}

}
