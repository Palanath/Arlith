package pala.apps.arlith.libraries.watchables;

public interface Watcher<V> {
	default void watch(V previousValue, V newValue, Watchable<? extends V> watchable) {
		watch(previousValue, newValue);
	}

	/**
	 * <p>
	 * Responds to a change of a {@link Watchable} that this {@link Watcher} has
	 * been watching. The {@link Watchable} is not known by implementors of this
	 * method. If it is needed, {@link #watch(Object, Object, Watchable)} needs to
	 * be overridden.
	 * </p>
	 * <p>
	 * This method should generally never be called except by
	 * {@link #watch(Object, Object, Watchable)}, as
	 * {@link #watch(Object, Object, Watchable)} is called by operations that change
	 * the value of a {@link Watchable}, and
	 * {@link #watch(Object, Object, Watchable)} calls
	 * {@link #watch(Object, Object)} if information pertaining to the
	 * {@link Watchable} that changed is not needed during response to the change.
	 * </p>
	 * 
	 * @param previousValue The previous value of the {@link Watchable}.
	 * @param newValue      The new value of the {@link Watchable}.
	 */
	void watch(V previousValue, V newValue);
}
