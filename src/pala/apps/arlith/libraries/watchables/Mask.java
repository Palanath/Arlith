package pala.apps.arlith.libraries.watchables;

import java.util.function.Function;

/**
 * A concrete implementation of {@link DerivativeWatchable} that takes in a
 * specified {@link Watchable} and a {@link Function} to convert the value of
 * that {@link Watchable} to the value of this {@link Mask}. This {@link Mask}
 * can be watched for changes, just as its source {@link Watchable} can be, but
 * the value provided to {@link Watcher}s of this {@link Mask} is transformed,
 * from the source {@link Watchable}'s value, by the {@link Function} specified
 * upon construction of this {@link Mask}. A {@link Mask}
 * 
 * @author Palanath
 *
 * @param <F> The type of the source {@link Watchable}.
 * @param <T> The type of this {@link Mask} as a {@link Watchable}.
 *            {@link Watcher} that see this {@link Mask}'s changes expect this
 *            type of value.
 */
public class Mask<F, T> extends DerivativeWatchable<F, T> {

	private final Function<? super F, ? extends T> conv;
	private final Watchable<? extends F> source;

	public Watchable<? extends F> getSource() {
		return source;
	}

	public Mask(Function<? super F, ? extends T> converter, Watchable<? extends F> source, boolean weak) {
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
