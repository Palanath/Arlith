package pala.apps.arlith.backend.client.api.caching.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
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

}
