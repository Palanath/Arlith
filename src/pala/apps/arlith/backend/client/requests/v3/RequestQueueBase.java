package pala.apps.arlith.backend.client.requests.v3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import pala.apps.arlith.backend.client.ClientNetworkingBase;
import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.Connection;

/**
 * <p>
 * A standard implementation of {@link RequestQueue}. This class keeps track of
 * a {@link #requestQueue request queue} and a {@link #queueThread queue thread}
 * which it uses to manage queing. Inquiries can be made on the calling thread
 * (as specified by {@link RequestSerializer} using {@link #inquire(Inquiry)},
 * or be made on the {@link #queueThread} (as specified by {@link RequestQueue})
 * {@link #queue(Inquiry, Consumer)} or
 * {@link #queue(Inquiry, Consumer, Consumer)}.
 * </p>
 * <p>
 * This class only instantiates a new {@link #queueThread queue thread} whenever
 * new requests are queued. If the queue thread is able to complete all the
 * requests in the queue, it blocks until a new item is put into the queue.
 * </p>
 * <p>
 * This class maintains a private {@link #connectionLock lock} that is used to
 * synchronize utilization of the {@link #getConnection() connection} provided
 * by {@link RequestSerializerBase}. The lock is synchronized over by the
 * {@link #queueThread queue thread} whenever it uses the
 * {@link #getConnection() connection} and by the {@link #inquire(Inquiry)}
 * method.
 * </p>
 * <p>
 * The {@link ClientNetworkingBase} class utilizes <code>this</code> object for
 * synchronizing {@link #start()} and {@link #stop()}. This class also
 * synchronizes over <code>this</code> when assuring that the queue thread is
 * prepared (and preparing it, if it isn't) so that the queued requests can be
 * made.
 * </p>
 * <p>
 * Note that this class does not implement request cancellation (via
 * {@link CompletableFuture#cancel(boolean)} on the {@link CompletableFuture}
 * returned by {@link #queueFuture(Inquiry)}), so calls to
 * {@link CompletableFuture#cancel(boolean)} on said {@link CompletableFuture}
 * will cancel the {@link CompletableFuture} object (and any
 * {@link CompletableFuture}s/threads waiting on its completion), but will not
 * actually stop the represented request from taking place.
 * </p>
 * 
 * @author Palanath
 *
 */
public abstract class RequestQueueBase extends RequestSerializerBase implements RequestQueue {

	/**
	 * Object used for synchronizing use of the {@link #getConnection() Connection}
	 * being used. This object is synchronized over in the {@link #queueThread} (see
	 * {@link #assureQueueThreadReady()}) and in {@link #inquire(Inquiry)}, both of
	 * which run {@link Inquiry Inquiries} using the {@link Connection}.
	 */
	private final Object connectionLock = new Object();

	private volatile Thread queueThread;
	protected final LinkedBlockingQueue<Request<?>> requestQueue = new LinkedBlockingQueue<>();

	/**
	 * <p>
	 * Called with {@link Throwable}s that occur while invoking
	 * <code>result handlers</code> and <code>error handlers</code> when performing
	 * requests. This method can be overridden to provide custom logging/error
	 * handling.
	 * </p>
	 * <p>
	 * By default, this method simply prints the error's stacktrace to the console.
	 * </p>
	 * <p>
	 * This method should not throw an exception.
	 * </p>
	 * 
	 * @param e The {@link Throwable} error that occurred.
	 */
	protected void handleException(Throwable e) {
		e.printStackTrace();
	}

	protected class Request<R> {
		private final Inquiry<? extends R> inquiry;
		private final Consumer<? super R> resultHandler;
		private final Consumer<? super Throwable> errorHandler;

		public Request(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler,
				Consumer<? super Throwable> errorHandler) {
			this.inquiry = inquiry;
			this.resultHandler = resultHandler;
			this.errorHandler = errorHandler;
		}

		/**
		 * <p>
		 * Performs this {@link Request}, passing the result to the
		 * {@link #resultHandler}, or the error to the {@link #errorHandler}, depending
		 * on the way in which the inquiry completes.
		 * </p>
		 * <p>
		 * Errors occurring from the call to the handler that gets executed are ignored.
		 * </p>
		 * 
		 * @throws Throwable
		 */
		public void perform() {
			R result;
			try {
				synchronized (connectionLock) {
					result = inquiry.inquire(getConnection());
				}
			} catch (Throwable e) {
				try {
					errorHandler.accept(e);
				} catch (Throwable e2) {
					handleException(e2);
				}
				return;
			}
			try {
				resultHandler.accept(result);
			} catch (Throwable e) {
				handleException(e);
			}
		}
	}

	@Override
	public <R> R inquire(Inquiry<? extends R> inquiry) throws CommunicationProtocolError {
		synchronized (connectionLock) {
			return super.inquire(inquiry);
		}
	}

	@Override
	public <R> void queue(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler,
			Consumer<? super Throwable> errorHandler) {
		try {
			requestQueue.put(new Request<R>(inquiry, resultHandler, errorHandler));
		} catch (InterruptedException e) {
		}
		assureQueueThreadReady();
	}

	protected synchronized void assureQueueThreadReady() {
		if (!isRunning())
			throw new IllegalStateException("Cannot run queued inquiries while the RequestQueueBase is stopped.");
		if (queueThread == null) {
			queueThread = new Thread(() -> {
				while (isRunning())
					try {
						requestQueue.take().perform();
					} catch (InterruptedException e) {
						if (!isRunning())
							return;
					}
			});
			queueThread.start();
		}

	}

	@Override
	public void stop() {
		super.stop();
		if (queueThread != null)
			queueThread.interrupt();
	}

}
