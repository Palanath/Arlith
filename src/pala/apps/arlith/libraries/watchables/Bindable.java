package pala.apps.arlith.libraries.watchables;

public interface Bindable<V> {
	void bind(Watchable<? extends V> watchable);

	void unbind();

	boolean isBound();
}
