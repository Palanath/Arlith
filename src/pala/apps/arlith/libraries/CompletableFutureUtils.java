package pala.apps.arlith.libraries;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.caching.v2.NewCache;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;

public final class CompletableFutureUtils {
	private CompletableFutureUtils() {
	}

	/**
	 * <p>
	 * A simple convenience function that acts as an analog to the
	 * <code>queue</code> method used commonly by the client's Request API (e.g. in
	 * {@link RequestQueue} and {@link NewCache}). This method takes two
	 * {@link Consumer}s, one that handles the success value of the computation
	 * represented by the specified {@link CompletableFuture}, and one that handles
	 * any error. This method differs from conventional queue methods in that it
	 * actually returns a {@link CompletableFuture} which allows the caller to deal
	 * with the result of the asynchronous computation after one of the specified
	 * handlers is invoked.
	 * </p>
	 * <p>
	 * This method is implemented by simply calling
	 * {@link CompletableFuture#handle(java.util.function.BiFunction)} and
	 * specifying a {@link BiFunction} that invokes the success handler if the error
	 * is <code>null</code>, and invokes the error handler otherwise. This method
	 * then returns the result of the {@link CompletableFuture#handle(BiFunction)}
	 * call.
	 * </p>
	 * <p>
	 * There are asynchronous analogs of this method in this class (which let the
	 * specified consumers be executed on a separate thread), notably:
	 * </p>
	 * <ul>
	 * <li>{@link #queueAsync(CompletableFuture, Consumer, Consumer)} and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Consumer, Consumer, Executor)}</li>
	 * </ul>
	 * <p>
	 * There are also other variants of this method that provide more power over
	 * this one:
	 * </p>
	 * <ul>
	 * <li>{@link #queue(CompletableFuture, Function, Function)},</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function)}, and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function, Executor)}</li>
	 * </ul>
	 * 
	 * @param <V>            The type of the result computation of the specified
	 *                       {@link CompletableFuture}. This is the type value
	 *                       returned by the computation represented by the
	 *                       {@link CompletableFuture}. (Once the asynchronous
	 *                       computation completes, it will return an instance of
	 *                       <code>V</code>.)
	 * @param future         The {@link CompletableFuture} to handle the result of.
	 * @param successHandler The {@link Consumer} to consume the <code>V</code>
	 *                       object that the {@link CompletableFuture} succeeds with
	 *                       (if it succeeds).
	 * @param errorHandler   The {@link Consumer} to consume the {@link Throwable}
	 *                       object that the {@link CompletableFuture} fails with
	 *                       (if it fails).
	 * @return A {@link CompletableFuture} that represents the computation of the
	 *         specified {@link CompletableFuture} and then its handling by the
	 *         appropriate {@link Consumer} specified.
	 */
	public static <V> CompletableFuture<Void> queue(CompletableFuture<? extends V> future,
			Consumer<? super V> successHandler, Consumer<? super Throwable> errorHandler) {
		return future.handle((t, u) -> {
			if (u == null)
				successHandler.accept(t);
			else
				errorHandler.accept(u);
			return null;
		});
	}

	/**
	 * <p>
	 * A simple convenience function that acts as an analog to the
	 * <code>queue</code> method used commonly by the client's Request API (e.g. in
	 * {@link RequestQueue} and {@link NewCache}). This method takes two
	 * {@link Consumer}s, one that handles the success value of the computation
	 * represented by the specified {@link CompletableFuture}, and one that handles
	 * any error. The {@link Consumer}s are called <b>asynchronously</b>; i.e., this
	 * method uses the specified {@link CompletableFuture}'s
	 * {@link CompletableFuture#handleAsync(BiFunction)} method. This method differs
	 * from conventional queue methods in that it actually returns a
	 * {@link CompletableFuture} which allows the caller to deal with the result of
	 * the asynchronous computation after one of the specified handlers is invoked.
	 * </p>
	 * <p>
	 * This method is implemented by simply calling
	 * {@link CompletableFuture#handleAsync(java.util.function.BiFunction)} and
	 * specifying a {@link BiFunction} that invokes the success handler if the error
	 * is <code>null</code>, and invokes the error handler otherwise. This method
	 * then returns the result of the
	 * {@link CompletableFuture#handleAsync(BiFunction)} call.
	 * </p>
	 * <p>
	 * There are asynchronous analogs of this method in this class (which let the
	 * specified consumers be executed on a separate thread), notably:
	 * </p>
	 * <ul>
	 * <li>{@link #queue(CompletableFuture, Consumer, Consumer)} and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Consumer, Consumer, Executor)}</li>
	 * </ul>
	 * <p>
	 * There are also other variants of this method that provide more power over
	 * this one:
	 * </p>
	 * <ul>
	 * <li>{@link #queue(CompletableFuture, Function, Function)},</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function)}, and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function, Executor)}</li>
	 * </ul>
	 * 
	 * @param <V>            The type of the result computation of the specified
	 *                       {@link CompletableFuture}. This is the type value
	 *                       returned by the computation represented by the
	 *                       {@link CompletableFuture}. (Once the asynchronous
	 *                       computation completes, it will return an instance of
	 *                       <code>V</code>.)
	 * @param future         The {@link CompletableFuture} to handle the result of.
	 * @param successHandler The {@link Consumer} to consume the <code>V</code>
	 *                       object that the {@link CompletableFuture} succeeds with
	 *                       (if it succeeds).
	 * @param errorHandler   The {@link Consumer} to consume the {@link Throwable}
	 *                       object that the {@link CompletableFuture} fails with
	 *                       (if it fails).
	 * @return A {@link CompletableFuture} that represents the computation of the
	 *         specified {@link CompletableFuture} and then its handling by the
	 *         appropriate {@link Consumer} specified.
	 */
	public static <V> CompletableFuture<Void> queueAsync(CompletableFuture<? extends V> future,
			Consumer<? super V> successHandler, Consumer<? super Throwable> errorHandler) {
		return future.handleAsync((t, u) -> {
			if (u == null)
				successHandler.accept(t);
			else
				errorHandler.accept(u);
			return null;
		});
	}

	/**
	 * <p>
	 * A simple convenience function that acts as an analog to the
	 * <code>queue</code> method used commonly by the client's Request API (e.g. in
	 * {@link RequestQueue} and {@link NewCache}). This method takes two
	 * {@link Consumer}s, one that handles the success value of the computation
	 * represented by the specified {@link CompletableFuture}, and one that handles
	 * any error. The {@link Consumer}s are called <b>asynchronously</b>, on the
	 * specified <code>Executor</code>; i.e., this method uses the specified
	 * {@link CompletableFuture}'s
	 * {@link CompletableFuture#handleAsync(BiFunction, Executor)} method. This
	 * method differs from conventional queue methods in that it actually returns a
	 * {@link CompletableFuture} which allows the caller to deal with the result of
	 * the asynchronous computation after one of the specified handlers is invoked.
	 * </p>
	 * <p>
	 * This method is implemented by simply calling
	 * {@link CompletableFuture#handleAsync(BiFunction, Executor)} and specifying a
	 * {@link BiFunction} that invokes the success handler if the error is
	 * <code>null</code>, and invokes the error handler otherwise. This method then
	 * returns the result of the
	 * {@link CompletableFuture#handleAsync(BiFunction, Executor)} call.
	 * </p>
	 * <p>
	 * There are asynchronous analogs of this method in this class (which let the
	 * specified consumers be executed on a separate thread), notably:
	 * </p>
	 * <ul>
	 * <li>{@link #queue(CompletableFuture, Consumer, Consumer)} and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Consumer, Consumer)}</li>
	 * </ul>
	 * <p>
	 * There are also other variants of this method that provide more power over
	 * this one:
	 * </p>
	 * <ul>
	 * <li>{@link #queue(CompletableFuture, Function, Function)},</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function)}, and</li>
	 * <li>{@link #queueAsync(CompletableFuture, Function, Function, Executor)}</li>
	 * </ul>
	 * 
	 * @param <V>            The type of the result computation of the specified
	 *                       {@link CompletableFuture}. This is the type value
	 *                       returned by the computation represented by the
	 *                       {@link CompletableFuture}. (Once the asynchronous
	 *                       computation completes, it will return an instance of
	 *                       <code>V</code>.)
	 * @param future         The {@link CompletableFuture} to handle the result of.
	 * @param successHandler The {@link Consumer} to consume the <code>V</code>
	 *                       object that the {@link CompletableFuture} succeeds with
	 *                       (if it succeeds).
	 * @param errorHandler   The {@link Consumer} to consume the {@link Throwable}
	 *                       object that the {@link CompletableFuture} fails with
	 *                       (if it fails).
	 * @param executor       The {@link Executor} to asynchronously execute the
	 *                       specified {@link Consumer} that gets invoked, on.
	 * @return A {@link CompletableFuture} that represents the computation of the
	 *         specified {@link CompletableFuture} and then its handling by the
	 *         appropriate {@link Consumer} specified.
	 */
	public static <V> CompletableFuture<Void> queueAsync(CompletableFuture<? extends V> future,
			Consumer<? super V> successHandler, Consumer<? super Throwable> errorHandler, Executor executor) {
		return future.handleAsync((t, u) -> {
			if (u == null)
				successHandler.accept(t);
			else
				errorHandler.accept(u);
			return null;
		}, executor);
	}

	public static <V, R> CompletableFuture<R> queue(CompletableFuture<? extends V> future,
			Function<? super V, ? extends R> successHandler, Function<? super Throwable, ? extends R> errorHandler) {
		return future.handle((t, u) -> {
			if (u == null)
				return successHandler.apply(t);
			else
				return errorHandler.apply(u);
		});
	}

	public static <V, R> CompletableFuture<R> queueAsync(CompletableFuture<? extends V> future,
			Function<? super V, ? extends R> successHandler, Function<? super Throwable, ? extends R> errorHandler) {
		return future.handleAsync((t, u) -> {
			if (u == null)
				return successHandler.apply(t);
			else
				return errorHandler.apply(u);
		});
	}

	public static <V, R> CompletableFuture<R> queueAsync(CompletableFuture<? extends V> future,
			Function<? super V, ? extends R> successHandler, Function<? super Throwable, ? extends R> errorHandler,
			Executor executor) {
		return future.handleAsync((t, u) -> {
			if (u == null)
				return successHandler.apply(t);
			else
				return errorHandler.apply(u);
		}, executor);
	}

	/**
	 * <p>
	 * Convenience function used by {@link ArlithClient} to allow blocking
	 * (synchronous) API methods to easily throw the types of
	 * {@link CommunicationProtocolError}s specified by their respective request.
	 * </p>
	 * <p>
	 * This method attempts to return <code>future.get()</code>, i.e., it attempts
	 * to return the result of the {@link CompletableFuture} provided. If the
	 * {@link CompletableFuture} did not complete correctly, the call to
	 * {@link CompletableFuture#get()} either results in an
	 * {@link InterruptedException} or an {@link ExecutionException}.
	 * </p>
	 * <ul>
	 * <li><b>If this method encounters an {@link InterruptedException},</b> it
	 * wraps the exception in a {@link RuntimeException} and throws the
	 * {@link RuntimeException}. Otherwise,</li>
	 * <li><b>If this method encounters an {@link ExecutionException},</b>
	 * (indicating that the execution of the asynchronous task represented by the
	 * {@link CompletableFuture} failed), this method acquires the cause of the
	 * {@link ExecutionException}.
	 * <ul>
	 * <li><b>If the cause is a {@link RuntimeException}</b>, the cause is thrown
	 * from this method.</li>
	 * <li><b>If the cause is an {@link Error},</b> the cause is thrown from this
	 * method.</li>
	 * <li><b>If the cause is an instance of the provided {@link Throwable}
	 * {@link Class},</b> the cause is thrown from this method.</li>
	 * <li><b>Otherwise,</b> the cause is wrapped in a {@link RuntimeException} and
	 * that {@link RuntimeException} is thrown from this method.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * Since this function specifies in its <code>throws</code> clause the generic
	 * type of exception provided by the caller, this method can conveniently allow
	 * callers to extract the value from a {@link CompletableFuture} without the
	 * need to include a <code>try</code>-<code>catch</code> construct
	 * (boilerplate), and still throw specific exceptions.
	 * </p>
	 * <p>
	 * An example use of this function is as follows:
	 * </p>
	 * 
	 * <pre>
	 * <code>public CompletableFuture&lt;Gift&gt; acquireGiftAsync() {
	 * 	return requestQueue.{@link RequestQueue#queueFuture(pala.apps.arlith.backend.client.requests.Inquiry) queueFuture}(new AcquireGiftRequest()).thenApply(Gift::fromNetworkGiftValue);
	 * }
	 * 
	 * public Gift acquireGift() throws NoGiftsGivenException, RuntimeException {
	 * 	return {@link CompletableFutureUtils}.getValue(acquireGiftAsync(), NoGiftsGivenException.class);
	 * }</code>
	 * </pre>
	 * 
	 * @param <V>    The type of the result of the {@link CompletableFuture} to
	 *               acquire the value from.
	 * @param <E1>   The type of {@link Exception} that the
	 *               {@link CompletableFuture} is expected to be able to encounter.
	 * @param future The {@link CompletableFuture}, representing some asychronous
	 *               computation.
	 * @param e1     {@link Class} instance of the class <code>E1</code>, used by
	 *               this method to check if an exception encountered by the
	 *               {@link CompletableFuture} is of the type specified.
	 * @return The result of the {@link CompletableFuture}.
	 * @throws RuntimeException If such an exception occurs during the asynchronous
	 *                          computation represented by the
	 *                          {@link CompletableFuture}, if an unspecified
	 *                          exception occurs during such computation, or if an
	 *                          {@link InterruptedException} is thrown by
	 *                          {@link CompletableFuture#get()}.
	 * @throws Error            If such an exception occurs during the asynchronous
	 *                          computation represented by the
	 *                          {@link CompletableFuture}.
	 * @throws E1               If such an exception occurs during the asynchronous
	 *                          computation represented by the
	 *                          {@link CompletableFuture}.
	 */
	public static <V, E1 extends Throwable> V getValue(CompletableFuture<? extends V> future, Class<? extends E1> e1)
			throws RuntimeException, Error, E1 {
		try {
			return future.get();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (ExecutionException e) {
			Throwable c = e.getCause();
			if (c instanceof RuntimeException)
				throw (RuntimeException) c;
			else if (c instanceof Error)
				throw (Error) c;
			else if (e1.isInstance(c))
				throw e1.cast(c);
			else
				throw new RuntimeException(c);
		}
	}

}
