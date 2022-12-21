package pala.apps.arlith.libraries.watchables;

import java.util.function.Function;

public class MaskBuilder<F, T> {
	private Function<? super F, ? extends T> converter;

	private MaskBuilder(Function<? super F, ? extends T> converter) {
		this.converter = converter;
	}

	public <X> MaskBuilder<F, X> then(Function<T, X> f) {
		return new MaskBuilder<>(converter.andThen(f));
	}

	public Mask<F, T> mask(Watchable<F> source) {
		return Mask.mask(source, converter);
	}

	public Mask<F, T> weakMask(Watchable<F> source) {
		return Mask.weakMask(source, converter);
	}

}
