package pala.apps.arlith.libraries.watchables;

public abstract class DerivativeWatchable<S, R> extends WatcherRegistry<R> implements Watcher<S> {

	private final Watcher<S> tw;

	public DerivativeWatchable(boolean weak) {
		tw = weak ? new WeakWatcher<>(this) : this;
	}

	/**
	 * Registers this {@link DerivativeWatchable} to whatever it is derived from.
	 * This method is needed because {@link DerivativeWatchable}s only register
	 * themselves to their sources and propagated changes whenever the
	 * {@link DerivativeWatchable}s have something watching them to propagate the
	 * changes to. There is no reason for a {@link DerivativeWatchable} to watch its
	 * sources if there is nothing watching the {@link DerivativeWatchable}.
	 * 
	 * @param tw "This" {@link DerivativeWatchable} as a watcher, or a wrapper of
	 *           this {@link DerivativeWatchable} (specifically, a
	 *           {@link WeakWatcher} wrapper), depending on how this
	 *           {@link DerivativeWatchable} was constructed. This parameter should
	 *           be registered to the sources of this {@link DerivativeWatchable}
	 *           (whatever this {@link DerivativeWatchable} was derived from)
	 *           instead of using <code>this</code>.
	 */
	protected abstract void registerThis(Watcher<S> tw);

	/**
	 * Unregisters this {@link DerivativeWatchable} from whatever thing(s) it was
	 * derived from. This method is needed to assure that
	 * {@link DerivativeWatchable}s are only watching their sources when the
	 * {@link DerivativeWatchable}s have something to propagate the sources' changes
	 * to; if nothing watches a {@link DerivativeWatchable}, it has no reason to
	 * watch its sources for changes.
	 * 
	 * @param tw "This" {@link DerivativeWatchable} as a watcher, or a wrapper of
	 *           this {@link DerivativeWatchable} (specifically, a
	 *           {@link WeakWatcher} wrapper), depending on how this
	 *           {@link DerivativeWatchable} was constructed. This parameter should
	 *           be unregistered from the sources of this
	 *           {@link DerivativeWatchable} (whatever this
	 *           {@link DerivativeWatchable} was derived from) instead of using
	 *           <code>this</code>.
	 */
	protected abstract void unregisterThis(Watcher<S> tw);

	/**
	 * <p>
	 * Propagates changes to the watchers of this {@link DerivativeWatchable},
	 * possibly masking the values being propagated (i.e. passing a modified version
	 * of the values. E.g., a {@link Mask} that adds <code>1</code> to its integer
	 * source would need to reflect this through its {@link #getValue()} method and
	 * through its {@link #watch(Object, Object)} method, when calling
	 * {@link #notifyWatchers(Object, Object)}).
	 * </p>
	 * <p>
	 * It is a requirement that this method be overridden if <code>S</code> and
	 * <code>R</code> are not of the same type and
	 * {@link #watch(Object, Object, Watchable)} is not overridden instead, as this
	 * method's default behavior is to simply cast the previous values propagated to
	 * it from the sources to <code>R</code> and pass the casted values to this
	 * object's watchers.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void watch(S previousValue, S newValue) {
		notifyWatchers((R) previousValue, (R) newValue);
	}

	@Override
	public final void register(Watcher<? super R> watcher) {
		if (watchers.isEmpty())
			registerThis(tw);
		super.register(watcher);
	}

	@Override
	public final void unregister(Watcher<? super R> watcher) {
		super.unregister(watcher);
		if (watchers.isEmpty())
			unregisterThis(tw);
	}

}
