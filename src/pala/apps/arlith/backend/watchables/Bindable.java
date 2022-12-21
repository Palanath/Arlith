package pala.apps.arlith.backend.watchables;

public interface Bindable<V> {
	void bind(Watchable<? extends V> watchable);

	void unbind();

	boolean isBound();
}
