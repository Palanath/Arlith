package pala.apps.arlith.api.watchables;

public interface Bindable<V> {
	void bind(Watchable<? extends V> watchable);

	void unbind();

	boolean isBound();
}
