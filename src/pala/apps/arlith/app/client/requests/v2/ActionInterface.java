package pala.apps.arlith.app.client.requests.v2;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.app.client.requests.Inquiry;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.execution.Action;

/**
 * <p>
 * Encapsulates an action that can be performed on a
 * {@link RequestSubsystemInterface}.
 * </p>
 * <p>
 * {@link ActionInterface}s can be made from an {@link Inquiry} using a
 * {@link RequestSubsystemInterface} or they can be built (chained) from other
 * {@link ActionInterface}s. Once an {@link ActionInterface} is built, it can be
 * executed by calling one of the appropriate methods.
 * </p>
 * <p>
 * {@link ActionInterface}s contain <i>chaining</i> methods that allow callers
 * to combine arbitrary code to be executed after the execution of an action,
 * possibly handling the result of or reason for failure of that action. These
 * methods return a new {@link ActionInterface} object that, when executed,
 * executes the original action and the auxiliary code. Unless explicitly
 * specified, chaining methods will only produce new {@link ActionInterface}s
 * that execute the specified code on the same thread as that that executes the
 * code represented by the source {@link ActionInterface}. Chaining methods
 * don't implicitly return a new {@link ActionInterface} that queues code on the
 * underlying {@link RequestSubsystemInterface}, either.
 * 
 * @author Palanath
 *
 * @param <R> The type of the result of the {@link ActionInterface}.
 */
public interface ActionInterface<R> {

	class DummyAction<R> implements ActionInterface<R> {
		private final R value;
		private final Exception exception;

		private DummyAction(R value) {
			this.value = value;
			exception = null;
		}

		@Override
		public RequestSubsystemInterface getRequestSubsystem() {
			return null;
		}

		private DummyAction(Exception exception) {
			this.exception = exception;
			value = null;
		}

		@Override
		public R get() throws CommunicationProtocolError, RuntimeException {
			if (exception != null)
				if (exception instanceof CommunicationProtocolError)
					throw (CommunicationProtocolError) exception;
				else
					throw (RuntimeException) exception;
			return value;
		}

		@Override
		public R poll() {
			return value;
		}

		@Override
		public Exception getException() {
			return exception;
		}

		@Override
		public Exception pollException() {
			return exception;
		}

		@Override
		public void queue() {
			throw new RuntimeException("Action already queued!");
		}

		@Override
		public <N> ActionInterface<N> then(Function<? super R, ? extends N> converter) {
			throw new RuntimeException("Action already queued!");
		}

		@Override
		public <N> ActionInterface<N> thenInquire(Function<? super R, ? extends Inquiry<N>> inquiryGenerator) {
			throw new RuntimeException("Action already queued!");
		}

		@Override
		public ActionInterface<R> handle(Function<? super Exception, ? extends R> exceptionHandler) {
			throw new RuntimeException("Action already queued!");
		}

	}

	static <V> ActionInterface<V> completed(V value) {
		return new DummyAction<>(value);
	}

	static <V> ActionInterface<V> completed(Exception exception) {
		return new DummyAction<>(exception);
	}

	/**
	 * Gets the {@link RequestSubsystemInterface} underlying this
	 * {@link ActionInterface}.
	 * 
	 * @return The {@link RequestSubsystemInterface} underlying this
	 *         {@link ActionInterface}.
	 */
	RequestSubsystemInterface getRequestSubsystem();

	/**
	 * <p>
	 * Submits this {@link ActionInterface} for execution on the
	 * {@link #getRequestSubsystem() underlying RequestSubsystem} to be executed
	 * either:
	 * <ul>
	 * <li>on this thread, or</li>
	 * <li>on a thread managed by the {@link RequestSubsystemInterface}, in which
	 * case this method will block until the {@link ActionInterface} completes.</li>
	 * </ul>
	 * In either case, this method will return (with a value of the result of the
	 * {@link ActionInterface}, or the exception encountered while running the
	 * {@link ActionInterface}, if any) once the {@link ActionInterface} completes,
	 * whether it completes normally or exceptionally.
	 * </p>
	 * <p>
	 * This method is <b>syncrhonous</b> and is used for blocking logic.
	 * </p>
	 * 
	 * @return The result of the {@link ActionInterface}.
	 */
	R get() throws CommunicationProtocolError, RuntimeException;

	/**
	 * Polls the successful result of this {@link ActionInterface}, if any, and
	 * returns it. Otherwise, returns <code>null</code>. This method does not block.
	 * If a response is not yet available, this method immediately returns
	 * <code>null</code>. Otherwise, it immediately returns the ready response. It
	 * does not throw any exceptions encountered while the {@link Action} was being
	 * performed. Those can be obtained with {@link #getException()} or
	 * {@link #pollException()}.
	 * 
	 * @return The value that this {@link Action} completed with, if any is yet
	 *         ready.
	 */
	R poll();

	/**
	 * <p>
	 * Analogous to {@link #get()}, but for the {@link Exception} returned by this
	 * {@link ActionInterface}, if any.
	 * </p>
	 * <p>
	 * This method will submit this {@link ActionInterface} to the
	 * {@link RequestSubsystemInterface} if it hasn't already been submitted, and
	 * will block until the {@link ActionInterface} has been completed. Once it is
	 * complete, this method returns the {@link Exception} that the
	 * {@link ActionInterface} encountered, if any. Otherwise, it returns
	 * <code>null</code>.
	 * </p>
	 * 
	 * @return The {@link Exception} encountered by the {@link ActionInterface}, if
	 *         any. Note that this can only ever be a subtype of {@link CommunicationProtocolError} or
	 *         {@link RuntimeException}. Other exceptions are either not handled by
	 *         {@link ActionInterface}s or are not stored by the
	 *         {@link ActionInterface} API once they are thrown.
	 */
	Exception getException();

	/**
	 * <p>
	 * Analogous to {@link #poll()}, but for the {@link Exception} returned by this
	 * {@link ActionInterface}, if any.
	 * </p>
	 * <p>
	 * Polls the exceptional result of this {@link ActionInterface}, if any, and
	 * returns it. Otherwise, returns <code>null</code>. This method does not block.
	 * If an exception is not yet available, this method immediately returns
	 * <code>null</code>. Otherwise, it immediately returns the exception thrown by
	 * this {@link ActionInterface}. It does not throw any exceptions.
	 * 
	 * @return The {@link Exception} thrown by the {@link ActionInterface}, if any.
	 *         Note that this can only ever be a subtype of {@link CommunicationProtocolError} or
	 *         {@link RuntimeException}. Other exceptions are either not handled by
	 *         {@link ActionInterface}s or are not stored by the
	 *         {@link ActionInterface} API once they are thrown.
	 */
	Exception pollException();

	/**
	 * Asynchronous method that returns <code>true</code> if this
	 * {@link ActionInterface} has completed exceptionally and <code>false</code>
	 * otherwise.
	 * 
	 * @return Whether this {@link ActionInterface} has completed exceptionally and
	 *         has an exception that can be obtained with {@link #pollException()}
	 *         or {@link #getException()}.
	 */
	default boolean hasException() {
		return pollException() != null;
	}

	/**
	 * Queues this {@link ActionInterface} for execution on the
	 * {@link #getRequestSubsystem() underlying RequestSubsystem} to be executed by
	 * a thread managed by the {@link RequestSubsystemInterface}.
	 */
	void queue();

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} and then calls the specified {@link Consumer} with
	 * the result. This is a chaining method.
	 * </p>
	 * <p>
	 * This method differs from {@link #then(Consumer)} by not changing the return
	 * type in the newly returned {@link ActionInterface}. The returned
	 * {@link ActionInterface}'s return type is the same as the return type of this
	 * {@link ActionInterface}.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason i.e. any
	 * exceptions are propagated.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Consumer}
	 * is called. The returned {@link ActionInterface} only succeeds if both this
	 * {@link ActionInterface} succeeds and the subsequent call to the provided
	 * {@link Consumer} succeeds, albeit the returned {@link ActionInterface}
	 * succeeds with the same return value of the .
	 * </p>
	 * 
	 * @param processor The processor to process the data with.
	 * @return A new, chained {@link ActionInterface}.
	 */
	default ActionInterface<R> process(Consumer<? super R> processor) {
		return then(a -> {
			processor.accept(a);
			return a;
		});
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} and then calls the specified {@link Consumer} with
	 * the result. This is a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason i.e. any
	 * exceptions are propagated.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Consumer}
	 * is called. The returned {@link ActionInterface} only succeeds if both this
	 * {@link ActionInterface} succeeds and the subsequent call to the provided
	 * {@link Consumer} succeeds, albeit the returned {@link ActionInterface}
	 * succeeds with no return value.
	 * </p>
	 * 
	 * @param resultHandler The {@link Consumer} that will receive the result of the
	 *                      {@link ActionInterface}.
	 * @return The new, chained {@link ActionInterface}.
	 */
	default ActionInterface<Void> then(Consumer<? super R> resultHandler) {
		return then(a -> {
			resultHandler.accept(a);
			return null;
		});
	}

	/**
	 * Alias for {@link #then(Consumer)}.
	 * 
	 * @param resultHandler The {@link Consumer} that will handle the result of this
	 *                      {@link ActionInterface}. See {@link #then(Consumer)} for
	 *                      more details.
	 * @return The new {@link ActionInterface}. See {@link #then(Consumer)} for more
	 *         details.
	 * @see #then(Consumer)
	 */
	default ActionInterface<Void> consume(Consumer<? super R> resultHandler) {
		return then(resultHandler);
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} then calls the specified {@link Function} with the
	 * result. This is a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason, i.e.
	 * any exceptions are propagated.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Function}
	 * is called. The returned {@link ActionInterface} only succeeds if both this
	 * {@link ActionInterface} succeeds and the subsequent call to the provided
	 * {@link Function} succeeds. The returned {@link ActionInterface} succeeds with
	 * the value returned by the provided {@link Function}.
	 * </p>
	 * 
	 * @param <N>       The type of value returned by the provided {@link Function}
	 *                  that will be fed the result of this {@link ActionInterface}.
	 * @param converter The {@link Function} that will be fed the result of this
	 *                  {@link ActionInterface}.
	 * @return The new, chained {@link ActionInterface}.
	 */
	<N> ActionInterface<N> then(Function<? super R, ? extends N> converter);

	/**
	 * Alias for {@link #then(Function)} for convenience when calling with a lambda
	 * expression argument. This method does nothing but immediately call and return
	 * {@link #then(Function)}.
	 * 
	 * @param <N>       The type of the value return by the provided
	 *                  {@link Function}. See {@link #then(Function)} for more
	 *                  details.
	 * @param converter The converter to apply to the result of this
	 *                  {@link ActionInterface} to generate the result of the
	 *                  returned {@link ActionInterface}. See
	 *                  {@link #then(Function)} for more details.
	 * @return The new {@link ActionInterface}. See {@link #then(Function)} for more
	 *         details.
	 * @see #then(Function)
	 */
	default <N> ActionInterface<N> transform(Function<? super R, ? extends N> converter) {
		return then(converter);
	}

	default ActionInterface<Void> ditchResult() {
		return then(a -> null);
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} then calls the specified {@link Supplier} and
	 * performs the resulting {@link Inquiry}. This is a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason, i.e.
	 * any exceptions are propagated. Otherwise, if calling the {@link Supplier}
	 * fails, the returned {@link ActionInterface} will fail exceptionally for the
	 * same reason. Otherwise, if the {@link Inquiry} made to the server fails, the
	 * returned {@link ActionInterface} will fail for the same reason.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Supplier}
	 * is called. If calling the {@link Supplier} succeeds, the resulting
	 * {@link Inquiry} is made to the server, and the result of the returned
	 * {@link ActionInterface} is the response value of that {@link Inquiry}.
	 * </p>
	 * <p>
	 * The returned {@link ActionInterface} only succeeds if all of the following
	 * hold true:
	 * <ol>
	 * <li>this {@link ActionInterface} succeeds,</li>
	 * <li>the subsequent call to the provided {@link Supplier} succeeds,</li>
	 * <li>the subsequently invoked {@link Inquiry} made to the server
	 * succeeds.</li>
	 * </ol>
	 * The returned {@link ActionInterface}, in such case, succeeds with the
	 * response returned by the server.
	 * </p>
	 * 
	 * @param <N>     The response type of the {@link Inquiry} to the server.
	 * @param inquiry The {@link Inquiry} to be made to the server after the
	 *                completion of this {@link ActionInterface}.
	 * @return The new, chained {@link ActionInterface}.
	 */
	default <N> ActionInterface<N> thenInquire(Supplier<? extends Inquiry<N>> inquiry) {
		return thenInquire(a -> inquiry.get());
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} then performs the resulting {@link Inquiry}. This is
	 * a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason, i.e.
	 * any exceptions are propagated. Otherwise, if the {@link Inquiry} made to the
	 * server fails, the returned {@link ActionInterface} will fail for the same
	 * reason.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Inquiry}
	 * is made to the server, and, if that {@link Inquiry} then succeeds, the result
	 * of the returned {@link ActionInterface} is the response value of that
	 * {@link Inquiry}.
	 * </p>
	 * <p>
	 * The returned {@link ActionInterface} only succeeds if both this
	 * {@link ActionInterface} succeeds and the {@link Inquiry} to the server
	 * succeeds. The returned {@link ActionInterface}, in such case, succeeds with
	 * the response to the provided {@link Inquiry}.
	 * </p>
	 * 
	 * @param <N>     The type of the response for the provided {@link Inquiry}.
	 * @param inquiry The {@link Inquiry} to make to the server after the completion
	 *                of this {@link ActionInterface}.
	 * @return The new, chained {@link ActionInterface}.
	 */
	default <N> ActionInterface<N> thenInquire(Inquiry<N> inquiry) {
		return thenInquire(() -> inquiry);
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} then calls the specified {@link Function} with the
	 * result and performs the {@link Inquiry} resulting from that {@link Function}
	 * call. This is a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally, then the returned
	 * {@link ActionInterface} also fails exceptionally for the same reason, i.e.
	 * any exceptions are propagated. Otherwise, if calling the {@link Function}
	 * fails, the returned {@link ActionInterface} will fail exceptionally for the
	 * same reason. Otherwise, if the {@link Inquiry} made to the server fails, the
	 * returned {@link ActionInterface} will fail for the same reason.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} succeeds, then the provided {@link Function}
	 * is called with the result of this {@link ActionInterface}'s success. If
	 * calling the {@link Function} succeeds, the resulting {@link Inquiry} is made
	 * to the server, and, if that {@link Inquiry} then succeeds, the result of the
	 * returned {@link ActionInterface} is the response value of that
	 * {@link Inquiry}.
	 * </p>
	 * <p>
	 * The returned {@link ActionInterface} only succeeds if all of the following
	 * hold true:
	 * <ol>
	 * <li>this {@link ActionInterface} succeeds,</li>
	 * <li>the subsequent call to the provided {@link Function} succeeds,</li>
	 * <li>the subsequently invoked {@link Inquiry} made to the server
	 * succeeds.</li>
	 * </ol>
	 * The returned {@link ActionInterface}, in such case, succeeds with the
	 * response returned by the server.
	 * </p>
	 * 
	 * @param <N>              The response type of the {@link Inquiry} to be made
	 *                         to the server.
	 * @param inquiryGenerator The {@link Function} to generate the {@link Inquiry}.
	 * @return The new, chained {@link ActionInterface}.
	 */
	<N> ActionInterface<N> thenInquire(Function<? super R, ? extends Inquiry<N>> inquiryGenerator);

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} and calls the specified {@link Consumer} with the
	 * exception if this {@link ActionInterface} completes exceptionally. This is a
	 * chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally then the {@link Consumer}
	 * provided is invoked with the {@link Exception}, rather than allowing the
	 * {@link Exception} to be propagated up to the
	 * {@link RequestSubsystemInterface}'s default exception handler. If the
	 * provided {@link Consumer} then completes normally, the returned
	 * {@link ActionInterface} also completes normally with the success value of
	 * <code>null</code>. If the provided {@link Consumer} fails exceptionally, then
	 * the returned {@link ActionInterface} fails with the same exception.
	 * </p>
	 * <p>
	 * Please note that since the provided {@link Consumer} will consume the
	 * exception, calling {@link #get()} or any of the exception acquiring methods
	 * on the returned {@link ActionInterface} will not throw or retrieve the
	 * exception.
	 * </p>
	 * 
	 * @param exceptionHandler The {@link Consumer} to handle any exception that
	 *                         occurs.
	 * @return The new, chained {@link ActionInterface}.
	 */
	default ActionInterface<R> handle(Consumer<? super Exception> exceptionHandler) {
		return handle(exceptionHandler, null);
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} and calls the specified {@link Consumer} with the
	 * exception if this {@link ActionInterface} completes exceptionally. This is a
	 * chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally then the {@link Consumer}
	 * provided is invoked with the {@link Exception}, rather than allowing the
	 * {@link Exception} to be propagated up to the
	 * {@link RequestSubsystemInterface}'s default exception handler. If the
	 * provided {@link Consumer} then completes normally, the returned
	 * {@link ActionInterface} also completes normally with the specified success
	 * value provided as the last argument to this method. If the provided
	 * {@link Consumer} fails exceptionally, then the returned
	 * {@link ActionInterface} fails with the same exception.
	 * </p>
	 * <p>
	 * Please note that since the provided {@link Consumer} will consume the
	 * exception, calling {@link #get()} or any of the exception acquiring methods
	 * on the returned {@link ActionInterface} will not throw or retrieve the
	 * exception.
	 * </p>
	 * 
	 * @param exceptionHandler       The {@link Consumer} to handle any exception
	 *                               that occurs.
	 * @param exceptionalReturnValue The value that the returned
	 *                               {@link ActionInterface} should complete with
	 *                               when an exception occurs and the provided
	 *                               {@link Consumer} handles it.
	 * @return The new, chained {@link ActionInterface}.
	 */
	default ActionInterface<R> handle(Consumer<? super Exception> exceptionHandler, R exceptionalReturnValue) {
		return handle(a -> {
			exceptionHandler.accept(a);
			return exceptionalReturnValue;
		});
	}

	/**
	 * <p>
	 * Returns a new {@link ActionInterface} that performs this
	 * {@link ActionInterface} and calls the specified {@link Function} with the
	 * exception if this {@link ActionInterface} completes exceptionally, in which
	 * case the result of the {@link Function} is the successful return type of the
	 * returned {@link ActionInterface}. This is a chaining method.
	 * </p>
	 * <p>
	 * If this {@link ActionInterface} fails exceptionally then the {@link Function}
	 * provided is invoked with the {@link Exception} as the argument, rather than
	 * allowing the {@link Exception} to be propagated up to the
	 * {@link RequestSubsystemInterface}'s default exception handler. If the
	 * provided {@link Function} then completes normally, the returned
	 * {@link ActionInterface} also completes normally with a success value of
	 * whatever the specified {@link Function} returns. If the provided
	 * {@link Function} fails exceptionally, then the returned
	 * {@link ActionInterface} fails with the same exception.
	 * </p>
	 * <p>
	 * Please note that since the provided {@link Function} will consume the
	 * exception, calling {@link #get()} or any of the exception acquiring methods
	 * on the returned {@link ActionInterface} will not throw or retrieve the
	 * exception.
	 * </p>
	 * 
	 * @param exceptionHandler The {@link Function} to handle any exception that
	 *                         occurs.
	 * @return The new, chained {@link ActionInterface}.
	 */
	ActionInterface<R> handle(Function<? super Exception, ? extends R> exceptionHandler);

}
