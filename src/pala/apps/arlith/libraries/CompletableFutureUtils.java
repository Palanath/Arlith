package pala.apps.arlith.libraries;

import static pala.libs.generic.JavaTools.array;
import static pala.libs.generic.JavaTools.hideCheckedExceptions;

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
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.JavaTools;

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
	 *                          {@link CompletableFuture}. This method's use comes
	 *                          from this type, and the {@link Class} provided in
	 *                          place of <code>e1</code>, being a checked exception.
	 */
	public static <V, E1 extends Throwable> V getValue(CompletableFuture<? extends V> future, Class<? extends E1> e1)
			throws RuntimeException, Error, E1 {
		return hideCheckedExceptions(() -> getValue(future, array(e1)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable> V getValue(CompletableFuture<? extends V> future,
			Class<? extends E1> e1, Class<? extends E2> e2) throws RuntimeException, Error, E1, E2 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3) throws RuntimeException, Error, E1, E2, E3 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4) throws RuntimeException, Error, E1, E2, E3, E4 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5)
			throws RuntimeException, Error, E1, E2, E3, E4, E5 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4, e5)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4, e5, e6)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7) throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4, e5, e6, e7)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7, Class<? extends E8> e8)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7, E8 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4, e5, e6, e7, e8)));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable> V getValue(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7, Class<? extends E8> e8, Class<? extends E9> e9)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7, E8, E9 {
		return hideCheckedExceptions(() -> getValue(future, array(e1, e2, e3, e4, e5, e6, e7, e8, e9)));
	}

	public static <V> V getValueWithDefaultExceptions(CompletableFuture<? extends V> future)
			throws RuntimeException, Error, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array());
	}

	/**
	 * <p>
	 * Synonymous with {@link #getValue(CompletableFuture, Class)}, but with
	 * already-specified, default exceptions:
	 * </p>
	 * <ul>
	 * <li>{@link SyntaxError}</li>
	 * <li>{@link ServerError}</li>
	 * <li>{@link RestrictedError}</li>
	 * <li>{@link RateLimitError}</li>
	 * <li style="color:
	 * #21726A;">{@link IllegalCommunicationProtocolException}</li>
	 * <li style="color:
	 * #21726A;">{@link CommunicationProtocolConstructionError}</li>
	 * </ul>
	 * <p>
	 * <span style="color: red;">Note</span> that the bottom two exceptions are
	 * {@link RuntimeException}s and are thus unchecked.
	 * </p>
	 * <p>
	 * Calling this method with exception class <code>E1</code> is equivalent to
	 * calling one of the {@link #getValue(CompletableFuture, Class)} method
	 * variants with <code>E1</code> and all four of the aforementioned default
	 * exception types.
	 * </p>
	 * 
	 * @param <V>    The result type of the {@link CompletableFuture} to extract the
	 *               value of.
	 * @param <E1>   The type of the specified exception to unwrap from the call to
	 *               {@link CompletableFuture#get()}.
	 * @param future The {@link CompletableFuture} to acquire the value of.
	 * @param e1     The {@link Class} type of the exception to unwrap.
	 * @return The result of the asynchronous action executed by the specified
	 *         {@link CompletableFuture}.
	 * @throws RuntimeException                       If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link RuntimeException} or a
	 *                                                checked exception is
	 *                                                encountered that was specified
	 *                                                to be unwrapped.
	 * @throws Error                                  If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters an
	 *                                                {@link Error}.
	 * @throws E1                                     If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters an
	 *                                                <code>E1</code>.
	 * @throws ServerError                            If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link ServerError}.
	 * @throws RestrictedError                        If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link RestrictedError}.
	 * @throws RateLimitError                         If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link RateLimitError}.
	 * @throws SyntaxError                            If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link SyntaxError}.
	 * @throws IllegalCommunicationProtocolException  If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters an
	 *                                                {@link IllegalCommunicationProtocolException}.
	 * @throws CommunicationProtocolConstructionError If the
	 *                                                {@link CompletableFuture}
	 *                                                computation encounters a
	 *                                                {@link CommunicationProtocolConstructionError}.
	 */
	public static <V, E1 extends Throwable> V getValueWithDefaultExceptions(CompletableFuture<? extends V> future,
			Class<? extends E1> e1) throws RuntimeException, Error, E1, ServerError, RestrictedError, RateLimitError,
			SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2)
			throws RuntimeException, Error, E1, E2, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3) throws RuntimeException, Error, E1, E2, E3, ServerError, RestrictedError,
			RateLimitError, SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4)
			throws RuntimeException, Error, E1, E2, E3, E4, ServerError, RestrictedError, RateLimitError, SyntaxError,
			IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, ServerError, RestrictedError, RateLimitError,
			SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4, e5));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, ServerError, RestrictedError, RateLimitError,
			SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4, e5, e6));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7, ServerError, RestrictedError, RateLimitError,
			SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4, e5, e6, e7));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7, Class<? extends E8> e8)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7, E8, ServerError, RestrictedError,
			RateLimitError, SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4, e5, e6, e7, e8));
	}

	public static <V, E1 extends Throwable, E2 extends Throwable, E3 extends Throwable, E4 extends Throwable, E5 extends Throwable, E6 extends Throwable, E7 extends Throwable, E8 extends Throwable, E9 extends Throwable> V getValueWithDefaultExceptions(
			CompletableFuture<? extends V> future, Class<? extends E1> e1, Class<? extends E2> e2,
			Class<? extends E3> e3, Class<? extends E4> e4, Class<? extends E5> e5, Class<? extends E6> e6,
			Class<? extends E7> e7, Class<? extends E8> e8, Class<? extends E9> e9)
			throws RuntimeException, Error, E1, E2, E3, E4, E5, E6, E7, E8, E9, ServerError, RestrictedError,
			RateLimitError, SyntaxError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return getValueWithDefaultExceptions(future, array(e1, e2, e3, e4, e5, e6, e7, e8, e9));
	}

	/**
	 * <p>
	 * Calls {@link #getValue(CompletableFuture, Class...)} with additional default
	 * exceptions:
	 * </p>
	 * <ul>
	 * <li>{@link SyntaxError}</li>
	 * <li>{@link ServerError}</li>
	 * <li>{@link RestrictedError}</li>
	 * <li>{@link RateLimitError}</li>
	 * <li>{@link IllegalCommunicationProtocolException}</li>
	 * <li>{@link CommunicationProtocolConstructionError}</li>
	 * </ul>
	 * <p>
	 * since these errors are commonly raised by almost every request applicable to
	 * {@link ArlithClient}.
	 * </p>
	 * <p>
	 * This method also hides checked exceptions, and so does not declare that it
	 * throws {@link Throwable}. Checked exceptions should be specified in the
	 * <code>throws</code> clauses of
	 * {@link #getValueWithDefaultExceptions(CompletableFuture, Class)} variants in
	 * this class (generically).
	 * </p>
	 * 
	 * @param <V>                The result type of the {@link CompletableFuture}.
	 * @param future             The {@link CompletableFuture} representing the
	 *                           asynchronous action to acquire the value of once
	 *                           complete.
	 * @param exceptionsToUnwrap The list of exceptions to unwrap from the
	 *                           {@link CompletableFuture}'s
	 *                           {@link CompletableFuture#get()} method call.
	 * @return The result of the {@link CompletableFuture}.
	 * @throws SyntaxError
	 * @throws ServerError
	 * @throws RestrictedError
	 * @throws RateLimitError
	 * @throws IllegalCommunicationProtocolException
	 * @throws CommunicationProtocolConstructionError
	 */
	@SafeVarargs
	private static <V> V getValueWithDefaultExceptions(CompletableFuture<? extends V> future,
			Class<? extends Throwable>... exceptionsToUnwrap) throws SyntaxError, ServerError, RestrictedError,
			RateLimitError, IllegalCommunicationProtocolException, CommunicationProtocolConstructionError {
		return hideCheckedExceptions(() -> getValue(future, JavaTools.combine(exceptionsToUnwrap, SyntaxError.class,
				ServerError.class, RestrictedError.class, RateLimitError.class)));
	}

	@SafeVarargs
	private static <V> V getValue(CompletableFuture<? extends V> future,
			Class<? extends Throwable>... exceptionsToUnwrap) throws Throwable {
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
			else
				for (Class<? extends Throwable> ex : exceptionsToUnwrap)
					if (ex.isInstance(c))
						throw ex.cast(c);
			throw new RuntimeException(c);
		}
	}

}
