package pala.apps.arlith.backend.client.requests.v3;

import java.util.function.Consumer;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;

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
	 * Queues an {@link Inquiry} to be made on this {@link RequestQueue}'s internal,
	 * dedicated thread. The {@link Inquiry} is added to the queue in the order that
	 * it is received so, {@link Inquiry inquiries} that are queued before others
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
	 * <li>If the {@link Inquiry} results in a {@link Throwable} exception of any
	 * kind, the exception is provided to the specified <code>errorHandler</code>.
	 * Otherwise, the result is provided to the <code>resultHandler</code>
	 * {@link Consumer}.</li>
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
