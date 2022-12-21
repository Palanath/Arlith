package pala.apps.arlith.backend.watchables;

import java.util.function.Function;

public class Mask<F, T> extends DerivativeWatchable<F, T> {

	private final Function<? super F, ? extends T> conv;
	private final Watchable<? extends F> source;

	public Watchable<? extends F> getSource() {
		return source;
	}

	private Mask(Function<? super F, ? extends T> converter, Watchable<? extends F> source, boolean weak) {
		super(weak);
		conv = converter;
		this.source = source;
	}

	public static <F, T> Mask<F, T> mask(Watchable<? extends F> source, Function<? super F, ? extends T> converter) {
		return new Mask<>(converter, source, false);
	}

	public static <F, T> Mask<F, T> weakMask(Watchable<? extends F> source,
			Function<? super F, ? extends T> converter) {
		return new Mask<>(converter, source, true);
	}

	public Function<? super F, ? extends T> getConverter() {
		return conv;
	}

	@Override
	public T getValue() {
		return conv.apply(source.getValue());// Delegates to source.
	}

	@Override
	public void watch(F previousValue, F newValue) {
		notifyWatchers(conv.apply(previousValue), conv.apply(newValue));
	}

	@Override
	protected void registerThis(Watcher<F> tw) {
		source.register(tw);
	}

	@Override
	protected void unregisterThis(Watcher<F> tw) {
		source.unregister(tw);
	}

}
