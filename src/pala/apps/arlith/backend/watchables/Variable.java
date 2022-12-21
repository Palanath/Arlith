package pala.apps.arlith.backend.watchables;

public class Variable<V> extends Property<V> implements Bindable<V>, Settable<V> {
	@Override
	protected void change(V newValue) {
		super.change(newValue);
	}

	public void set(V newValue) {
		if (isBound())
			throw new RuntimeException("Cannot set the value of a Variable that is bound.");
		change(newValue);
	}

	public class Hook extends Property<V>.Hook {

		protected Hook(Watchable<? extends V> other) {
			super(other);
		}

		@Override
		public void unhook() {
			super.unhook();
		}

		@Override
		public Watchable<? extends V> getWatchable() {
			return super.getWatchable();
		}

	}

	public Variable() {
	}

	public Variable(V initialValue) {
		super(initialValue);
	}

	@Override
	public Hook getHook(Watchable<? extends V> watchable) {
		return (Hook) super.getHook(watchable);
	}

	@Override
	public Hook hook(Watchable<? extends V> other) {
		if (isBound())
			throw new RuntimeException("Cannot hook a bound variable.");
		return new Hook(other);
	}

	@Override
	public void unhook(Watchable<? extends V> other) {
		super.unhook(other);
	}

	@Override
	public void bind(Watchable<? extends V> watchable) {
		super.bind(watchable);
	}

	@Override
	public void unbind() {
		super.unbind();
	}

	@Override
	public boolean isBound() {
		return super.isBound();
	}
}
