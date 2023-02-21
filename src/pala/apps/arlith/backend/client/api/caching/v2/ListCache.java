package pala.apps.arlith.backend.client.api.caching.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.types.ListValue;

public class ListCache<O> extends NewCache<List<O>> {

	public static <F, T, A extends List<T>> Function<ListValue<? extends F>, A> listfn(
			Function<? super F, ? extends T> converter, Function<? super Integer, ? extends A> listMaker) {
		return a -> {
			A l = listMaker.apply(a.size());
			for (F f : a)
				l.add(converter.apply(f));
			return l;
		};
	}

	public ListCache(List<O> value) {
		super(value);
	}

	public <T> ListCache(Supplier<? extends Inquiry<? extends ListValue<? extends T>>> inquirySupplier,
			Function<? super T, ? extends O> listValueConverter, RequestQueue requestQueue) {
		super(inquirySupplier, listfn(listValueConverter, ArrayList::new), requestQueue);
	}

	public <T> ListCache(Inquiry<? extends ListValue<? extends T>> inquiry,
			Function<? super T, ? extends O> listValueConverter, RequestQueue requestQueue) {
		this(() -> inquiry, listValueConverter, requestQueue);
	}

	public ListCache(Inquiry<? extends ListValue<? extends O>> inquiry, RequestQueue requestQueue) {
		this(() -> inquiry, a -> a, requestQueue);
	}

	public ListCache(Supplier<? extends Inquiry<? extends ListValue<? extends O>>> inquirySupplier,
			RequestQueue requestQueue) {
		this(inquirySupplier, a -> a, requestQueue);
	}

	/**
	 * Same as {@link #get()}, but the result is unmodifiable. This method calls
	 * {@link #get()} and, if the result is not <code>null</code>, calls
	 * {@link Collections#unmodifiableList(List)} and returns the result of that. If
	 * the result of {@link #get()} is <code>null</code>, this method returns
	 * <code>null</code>.
	 * 
	 * @return The list, but unmodifiable.
	 * @throws CommunicationProtocolError Iff {@link #get()} throws a
	 *                                    {@link CommunicationProtocolError}.
	 */
	public List<O> getUnmodifiable() throws CommunicationProtocolError {
		List<O> value = get();
		return value == null ? null : Collections.unmodifiableList(value);
	}

	/**
	 * <p>
	 * Same as {@link #queue(Consumer, Consumer)}, but the {@link List} provided to
	 * the <code>resultHandler</code> is unmodifiable. If querying the cache results
	 * in the value <code>null</code> then <code>null</code> is provided to the
	 * <code>resultHandler</code>. Otherwise, the list retrieved from the cache is
	 * passed to {@link Collections#unmodifiableList(List)}, and the result of that
	 * is given to <code>resultHandler</code>.
	 * </p>
	 * <p>
	 * Note that the cache query may cause the cache to try to populate itself,
	 * which involves sending a request.
	 * </p>
	 * 
	 * @param resultHandler The {@link Consumer} to handle the result of the cache
	 *                      query (either <code>null</code> or an unmodifiable
	 *                      {@link List}). The {@link Consumer} can be
	 *                      <code>null</code>, in which case, the result is
	 *                      discarded.
	 * @param errorHandler  The {@link Consumer} to handle the error. The error
	 *                      handler can be <code>null</code>, in which case any
	 *                      errors are simply discarded.
	 */
	public void getUnmodifiable(Consumer<? super List<O>> resultHandler, Consumer<? super Throwable> errorHandler) {
		super.queue(a -> resultHandler.accept(a == null ? null : Collections.unmodifiableList(a)), errorHandler);
	}

	/**
	 * Calls {@link #getUnmodifiable(Consumer, Consumer)}, but passes
	 * <code>null</code> for the <code>errorHandler</code>.
	 * 
	 * @param resultHandler The {@link Consumer} to handle the result of the cache
	 *                      query (the result being either <code>null</code> or an
	 *                      unmodifiable {@link List}). The {@link Consumer} can be
	 *                      <code>null</code>, in which case the result is
	 *                      discarded.
	 */
	public void getUnmodifiable(Consumer<? super List<O>> resultHandler) {
		super.queue(a -> resultHandler.accept(a == null ? null : Collections.unmodifiableList(a)));
	}

}
