package pala.apps.arlith.backend.client.api.caching.v2;

import java.lang.ref.WeakReference;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.libraries.watchables.View;
import pala.apps.arlith.libraries.watchables.Watchable;
import pala.apps.arlith.libraries.watchables.Watcher;
import pala.apps.arlith.libraries.watchables.WatcherRegistry;

/**
 * A {@link NewCache} implementation that supports the {@link Watchable}
 * interface. {@link WatchableCache}s can have {@link Watcher}s registered to
 * them which get notified whenever the {@link WatchableCache} is first
 * populated or its cached value is subsequently updated.
 * 
 * @author Palanath
 *
 * @param <V> The type of value held by this cache.
 */
public class WatchableCache<V> extends NewCache<V> implements Watchable<V> {

	private final WR registry = new WR();

	public WatchableCache(V value) {
		super(value);
	}

	public WatchableCache(Inquiry<? extends V> inquiry, RequestQueue requestQueue) {
		super(inquiry, requestQueue);
	}

	public <T> WatchableCache(Inquiry<? extends T> inquiry, Function<? super T, ? extends V> resultConverter,
			RequestQueue requestQueue) {
		super(inquiry, resultConverter, requestQueue);
	}

	public WatchableCache(Supplier<? extends Inquiry<? extends V>> inquirySupplier, RequestQueue requestQueue) {
		super(inquirySupplier, requestQueue);
	}

	public <T> WatchableCache(Supplier<? extends Inquiry<? extends T>> inquirySupplier,
			Function<? super T, ? extends V> resultConverter, RequestQueue requestQueue) {
		super(inquirySupplier, resultConverter, requestQueue);
	}

	@Override
	public synchronized void updateItem(V item) {
		V old = getIfPopulated();
		super.updateItem(item);
		registry.notifyWatchers(old, item);
	}

	/**
	 * Returns {@link #getIfPopulated()}. Gets and returns the value of this
	 * {@link WatchableCache} if it has been populated. Otherwise, returns
	 * <code>null</code>.
	 */
	@Override
	public V getValue() {
		return getIfPopulated();
	}

	private class WR extends WatcherRegistry<V> {

		@Override
		public V getValue() {
			return WatchableCache.this.getValue();
		}

		@Override
		protected synchronized void notifyWatchers(V oldValue, V newValue) {
			super.notifyWatchers(oldValue, newValue);
		}

	}

	@Override
	public void register(Watcher<? super V> watcher) {
		registry.register(watcher);
	}

	@Override
	public void unregister(Watcher<? super V> watcher) {
		registry.unregister(watcher);
	}

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

}
