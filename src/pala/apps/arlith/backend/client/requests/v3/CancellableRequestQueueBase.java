package pala.apps.arlith.backend.client.requests.v3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.libs.generic.util.Box;

public abstract class CancellableRequestQueueBase extends RequestQueueBase {
	protected class CancellableRequest<R> extends Request<R> {
		private boolean cancelled;

		@Override
		public void perform() {
			if (!cancelled)
				super.perform();
		}

		public CancellableRequest(Inquiry<? extends R> inquiry, Consumer<? super R> resultHandler,
				Consumer<? super Throwable> errorHandler) {
			super(inquiry, resultHandler, errorHandler);
		}
	}

	@Override
	public <R> CompletableFuture<R> queueFuture(Inquiry<? extends R> inquiry) {
		Box<CancellableRequest<? extends R>> r = new Box<>();
		CompletableFuture<R> future = new CompletableFuture<R>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				r.value.cancelled = true;
				return super.cancel(mayInterruptIfRunning);
			}
		};
		r.value = new CancellableRequest<>(inquiry, future::complete, future::completeExceptionally);
		try {
			requestQueue.put(r.value);
		} catch (InterruptedException e) {
		}
		assureQueueThreadReady();
		return future;
	}
}
