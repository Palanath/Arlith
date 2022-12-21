package pala.apps.arlith.backend.client.requests;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Action<R> {
	R perform();

	static Action<Void> from(Runnable runnable) {
		return () -> {
			runnable.run();
			return null;
		};
	}

	static <R> Action<R> from(Supplier<R> supplier) {
		return supplier::get;
	}

	/**
	 * Returns a new {@link Action} that performs this action and then the specified
	 * {@link Action} (i.e. this action before the specified action), returning the
	 * result from the specified {@link Action}.
	 * 
	 * @param other The {@link Action} to perform after this {@link Action}.
	 * @param <A>   The type of the second {@link Action} (and of the result of this
	 *              method call).
	 * @return The new {@link Action}.
	 */
	default <A> Action<A> before(Action<? extends A> other) {
		return () -> {
			perform();
			return other.perform();
		};
	}

	/**
	 * Returns a new {@link Action} that performs the specified action and then this
	 * action, returning the reuslt of this {@link Action}.
	 * 
	 * @param other The other {@link Action} to perform (before this one).
	 * @return The new chain {@link Action}.
	 */
	default Action<R> after(Action<?> other) {
		return () -> {
			other.perform();
			return perform();
		};
	}

	/**
	 * Returns an {@link Action} that performs this {@link Action} then sends the
	 * result to the provided {@link Function}. The returned {@link Action} calls
	 * this {@link Action} then the provided {@link Function}.
	 * 
	 * @param <T>      The type of the resulting {@link Action}
	 * @param nextLink The next link in the chain. Specifically, the next thing to
	 *                 perform after this {@link Action} is finished.
	 * @return The resulting {@link Action}.
	 */
	default <T> Action<T> chain(Function<? super R, ? extends T> nextLink) {
		return () -> nextLink.apply(perform());
	}

	default CompletableFuture<R> execute(ExecutorService service) {
		return CompletableFuture.supplyAsync(toSupplier(), service);
	}

	default Supplier<R> toSupplier() {
		return this::perform;
	}

}
