package pala.apps.arlith.backend.client.requests.v3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;

/**
 * <p>
 * A {@link RequestSerializer} that additionally supports queueing
 * {@link Inquiry inquiries} to be made on a {@link RequestQueue}-dedicated
 * {@link Thread}. Calls to {@link #inquire(Inquiry)} are made on the calling
 * thread and cause the calling thread to block until complete, whereas calls to
 * {@link #queue(Inquiry, Consumer)} and
 * {@link #queue(Inquiry, Consumer, Consumer)} submit the {@link Inquiry} to be
 * made to an internal request queue, requests from which are handled one at a
 * time, in sequence, by a dedicated thread.
 * </p>
 * <p>
 * Queue methods provided by this class optionally allow the caller to specify
 * result and error handlers, which process the results of the request on the
 * iternal, dedicated thread. It is recommended that these handlers be
 * lightweight and not perform blocking operations, as they are executed on this
 * {@link RequestQueue}'s dedicated thread and such operations can siphon
 * execution time from other queued requests, waiting to be completed. If
 * substantial further processing needs to be completed, handlers may notify
 * another, processing thread, once the request has been made, with the result
 * (or error), and then immediately return so that the dedicated thread is free
 * to handle the next, queued request.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestQueue extends RequestSerializer {

	/**
	 * <p>
	 * Returns a {@link CompletableFuture} that represents the asynchronous making
	 * of the specified {@link Inquiry} to the server. The {@link CompletableFuture}
	 * is completed, either exceptionally or normally, whenever the call to
	 * {@link #queue(Inquiry, Consumer, Consumer)} completes in the likewise manner.
	 * </p>
	 * <p>
	 * <span style="color: red;">Note that {@link CompletableFuture}s returned by
	 * {@link RequestQueue} implementations cannot be
	 * {@link CompletableFuture#cancel(boolean) cancelled} unless otherwise
	 * specified,</span> <b>and that</b> {@link CompletableFuture#cancel(boolean)}
	 * should not be called on results returned from this method (unless allowed by
	 * the implementation); <span style="color: red;"><b>calls to the
	 * {@link CompletableFuture#cancel(boolean)} method may invoke undefined
	 * behavior.</b></span>
	 * </p>
	 * <p>
	 * The default implementation of this method creates a new
	 * {@link CompletableFuture} and calls
	 * {@link #queue(Inquiry, Consumer, Consumer)}; the result handler provided to
	 * {@link #queue(Inquiry, Consumer, Consumer)} completes the future with the
	 * result value, and the exception handler provided completes the future with an
	 * exceptional value. (There is no way for such calls to
	 * {@link #queue(Inquiry, Consumer, Consumer)} to become aware that the
	 * {@link CompletableFuture}, created in and returned by this method, is
	 * cancelled, if it ever is, which is why care should be taken when passing
	 * around {@link CompletableFuture}s returned by this method).
	 * </p>
	 * <p>
	 * Implementations are free to implement
	 * {@link CompletableFuture#cancel(boolean)}, either with or without regard to
	 * the method's parameter.
	 * </p>
	 * 
	 * @param <R>     The type of the result of the {@link Inquiry} being made.
	 * @param inquiry The {@link Inquiry} to be made to the server.
	 * @return A {@link CompletableFuture} that represents the result of invoking
	 *         the {@link Inquiry}. Note that the default implementation of this
	 *         method does not actually cancel the request being made to the server
	 *         if {@link CompletableFuture#cancel(boolean)} is called (the default
	 *         implementation of {@link CompletableFuture#cancel(boolean)} is used,
	 *         so threads waiting on the {@link CompletableFuture} to complete will
	 *         be interrupted by reason of cancellation, and other
	 *         {@link CompletableFuture}s that branch from the returned future will
	 *         receive notice of the cancellation, as expected).
	 */
	default <R> CompletableFuture<R> queueFuture(Inquiry<? extends R> inquiry) {
		CompletableFuture<R> future = new CompletableFuture<>();
		queue(inquiry, future::complete, future::completeExceptionally);
		return future;
	}

	/**
	 * <p>
	 * Queues an {@link Inquiry} to be made on this {@link RequestQueue}'s internal,
	 * dedicated thread. The {@link Inquiry} is added to the queue in the order that
	 * it is received, so {@link Inquiry inquiries} that are queued before others
	 * are made before others.
	 * </p>
	 * <p>
	 * The result of the {@link Inquiry} is ignored, and so are any exceptions if
	 * the {@link Inquiry} raises an exception.
	 * </p>
	 * 
	 * @param inquiry The {@link Inquiry} to make to the server.
	 */
	default void queue(Inquiry<?> inquiry) {
		queue(inquiry, null);
	}

	/**
	 * <p>
	 * Queues an {@link Inquiry} to be made on this {@link RequestQueue}'s internal,
	 * dedicated thread. The {@link Inquiry} is added to the queue in the order that
	 * it is received, so {@link Inquiry inquiries} that are queued before others
	 * are made before others. The provided {@link Consumer} is invoked once the
	 * {@link Inquiry} completes to handle the result. The {@link Consumer} is
	 * invoked on the same thread used for making requests to the server, and queued
	 * requests will not be run until the consumer returns and the thread is freed.
	 * </p>
	 * <ul>
	 * <li>If the {@link Inquiry} results in an exception of any kind, the exception
	 * is discarded and the provided {@link Consumer} is not invoked. For exception
	 * handling, see {@link #queue(Inquiry, Consumer, Consumer)}.</li>
	 * <li>If the provided {@link Consumer} results in an exception, it is
	 * ignored.</li>
	 * </ul>
	 * 
	 * @param <R>           The type of result of the {@link Inquiry}.
	 * @param inquiry       The {@link Inquiry} to be made to the server.
	 * @param resultHandler A {@link Consumer} that handles the result of the
	 *                      {@link Inquiry}.
	 */
	default <R> void queue(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler) {
		queue(inquiry, resultHandler, null);
	}

	/**
	 * <p>
	 * Queues an {@link Inquiry} to be made on this {@link RequestQueue}'s internal,
	 * dedicated thread. The {@link Inquiry} is added to the queue in the order that
	 * it is received, so {@link Inquiry inquiries} that are queued before others
	 * are made before others.
	 * </p>
	 * <p>
	 * The provided {@link Consumer}s are invoked once the {@link Inquiry}
	 * completes, either normally or exceptionally, to handle the result (response
	 * or exception). If the {@link Inquiry} results in an {@link Throwable} of any
	 * kind, it is provided to the specified <code>errorHandler</code>. Otherwise
	 * the {@link Inquiry} completes normally, in qhich case the response is
	 * provided to the specified <code>resultHandler</code> {@link Consumer}.
	 * </p>
	 * <p>
	 * The {@link Consumer} that is invoked is executed on the same thread that's
	 * used for handling queued inquiries (on the same thread that the request was
	 * made on), so other queued requests will not be run until the {@link Consumer}
	 * returns and the thread is free.
	 * </p>
	 * </p>
	 * <ul>
	 * <li>If the {@link Inquiry} itself raises a {@link Throwable} exception of any
	 * kind, the exception is provided to the specified <code>errorHandler</code>.
	 * Otherwise, the result is provided to the <code>resultHandler</code>
	 * {@link Consumer}. Note that {@link UnknownCommStateException}s and
	 * {@link BlockException}s are <i>captured and handled by the
	 * {@link RequestQueue}</i> and are thus <b>never</b> provided to the exception
	 * handler.</li>
	 * <li>If the {@link Consumer} that is invoked results in an exception, the
	 * excpetion is ignored.</li>
	 * </ul>
	 * <p>
	 * The only {@link CommunicationProtocolError}s that can occur during execution
	 * (and thus, passed to the provided <code>errorHandler</code>
	 * {@link Consumer}), are those that are thrown by calling
	 * {@link Inquiry#inquire(pala.apps.arlith.libraries.networking.scp.CommunicationConnection)}
	 * on the specified {@link Inquiry}. Additionally, an
	 * {@link IllegalCommunicationProtocolException} will only ever be provided to
	 * the <code>errorHandler</code> if the {@link Inquiry} throws it, as well.
	 * </p>
	 * 
	 * @param <R>           The response type of the {@link Inquiry}.
	 * @param inquiry       The {@link Inquiry} to be made to the server.
	 * @param resultHandler A {@link Consumer} used to handle the response, or
	 *                      <code>null</code> if the response should be ignored.
	 * @param errorHandler  A {@link Consumer} used to handle any error that occurs
	 *                      during execution, or <code>null</code> if errors should
	 *                      be ignored.
	 */
	<R> void queue(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler,
			Consumer<? super Throwable> errorHandler);
}
