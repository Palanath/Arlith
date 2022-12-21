package pala.apps.arlith.backend.watchables;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public abstract class BindableImpl<S> implements Bindable<S>, Settable<S> {

	public static <S> BindableImpl<S> bindable(Consumer<? super S> handler) {
		return new BindableImpl<S>() {
			@Override
			public void set(S value) {
				handler.accept(value);
			}
		};
	}

	/**
	 * The thing we watch for changes. A weak reference to this {@link Watchable} is
	 * held because this class's implementation assumes that no changing of the
	 * object through this reference will take place. Therefore, once no other
	 * reference to the object exists, the object will cease to be changed, and our
	 * watching it will be useless.
	 */
	private WeakReference<Watchable<? extends S>> watchable;
	private Watcher<S> wchr = (previousValue, newValue) -> set(newValue);

	@Override
	public void bind(Watchable<? extends S> watchable) {
		if (this.watchable != null) {
			Watchable<? extends S> w = this.watchable.get();
			if (w != null)
				throw new RuntimeException("Already bound.");
		}
		watchable.register(wchr);
		set(watchable.getValue());
		this.watchable = new WeakReference<>(watchable);
	}

	public @Override boolean isBound() {
		return watchable != null && watchable.get() != null;
	}

	@Override
	public void unbind() {
		if (watchable != null) {
			Watchable<? extends S> w = watchable.get();
			if (w != null)
				w.unregister(wchr);
		}
		watchable = null;
	}

}
