package pala.apps.arlith.backend.watchables;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class Property<V> extends WatcherRegistry<V> implements Watchable<V> {

	private WeakReference<View<V>> view;

	public View<V> getView() {
		if (view != null) {
			View<V> v = view.get();
			if (v != null)
				return v;
		}
		View<V> v = View.view(this);
		view = new WeakReference<>(v);
		return v;
	}

	private final BindableImpl<V> bindImpl = BindableImpl.bindable(this::change);

	private V value;

	@Override
	public V getValue() {
		return value;
	}

	protected void change(V newValue) {
		V prev = value;
		value = newValue;
		notifyWatchers(prev, newValue);
	}

	/**
	 * Sets an initial value for this {@link Property}. This is the only way to set
	 * a value to this property without notifying watchers.
	 * 
	 * @param initialValue The initial value of the {@link Property}.
	 */
	public Property(V initialValue) {
		value = initialValue;
	}

	public Property() {
	}

	private final Map<Watchable<? extends V>, Hook> hooks = new HashMap<>();

	public class Hook {
		private final Watchable<? extends V> watchable;
		private final Watcher<V> watcher = this::watch;

		protected void unhook() {
			watchable.unregister(watcher);
			hooks.remove(watchable);
		}

		protected Watchable<? extends V> getWatchable() {
			return watchable;
		}

		public Property<V> getProperty() {
			return Property.this;
		}

		protected Hook(Watchable<? extends V> watchable) {
			(this.watchable = watchable).register(watcher);
			hooks.put(watchable, this);
		}

		protected void watch(V previousValue, V newValue) {
			change(newValue);
		}

	}

	protected Hook getHook(Watchable<? extends V> watchable) {
		return hooks.get(watchable);
	}

	protected void unhook(Watchable<? extends V> other) {
		Hook h = hooks.get(other);
		if (h != null)
			h.unhook();
	}

	protected Hook hook(Watchable<? extends V> other) {
		if (hooks.containsKey(other))
			throw new RuntimeException("Watchable already hooked!");
		return new Hook(other);
	}

	protected void bind(Watchable<? extends V> watchable) {
		bindImpl.bind(watchable);
	}

	protected void unbind() {
		bindImpl.unbind();
	}

	protected boolean isBound() {
		return bindImpl.isBound();
	}

}
