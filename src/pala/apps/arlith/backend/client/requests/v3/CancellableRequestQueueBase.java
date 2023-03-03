package pala.apps.arlith.backend.client.requests.v3;

import java.util.concurrent.CompletableFuture;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.libs.generic.util.Box;

public abstract class CancellableRequestQueueBase extends RequestQueueBase {

	/**
	 * Returns a {@link CompletableFuture} that represents the asynchronous action
	 * of making the specified {@link Inquiry} to the server over this
	 * {@link CancellableRequestQueueBase}. The returned {@link CompletableFuture}'s
	 * {@link CompletableFuture#cancel(boolean)} method may be called at any time.
	 * If it is invoked and the {@link Inquiry} is waiting in the
	 * {@link RequestQueue}'s inquiry queue, to be made to the server, then the
	 * {@link Inquiry} is unqueued and the method returns a value as per its
	 * default, {@link CompletableFuture} implementation. Otherwise, if the
	 * {@link Inquiry} has already been taken from the queue, the cancel method
	 * returns <code>false</code> and does nothing.
	 */
	@Override
	public <R> CompletableFuture<R> queueFuture(Inquiry<? extends R> inquiry) {
		Box<Request<? extends R>> r = new Box<>();
		CompletableFuture<R> future = new CompletableFuture<R>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if (requestQueue.remove(r.value))
					return super.cancel(mayInterruptIfRunning);
				else
					return false;
			}
		};
		r.value = new Request<>(inquiry, future::complete, future::completeExceptionally);
		try {
			requestQueue.put(r.value);
		} catch (InterruptedException e) {
		}
		assureQueueThreadReady();
		return future;
	}
}
