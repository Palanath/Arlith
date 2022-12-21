package pala.apps.arlith.backend.watchables;

import java.util.function.Function;

public interface Watchable<V> {

	V getValue();

	void register(Watcher<? super V> watcher);

	void unregister(Watcher<? super V> watcher);

	default <X> Mask<V, X> expression(Function<V, X> converter) {
		return Mask.mask(this, converter);
	}

	default <X> Mask<V, X> weakExpression(Function<V, X> converter) {
		return Mask.weakMask(this, converter);
	}

}