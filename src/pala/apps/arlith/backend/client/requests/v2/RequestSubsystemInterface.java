package pala.apps.arlith.backend.client.requests.v2;

import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;

public interface RequestSubsystemInterface {
	/**
	 * Produces an {@link ActionInterface} that represents the act of making the
	 * specified {@link Inquiry} to the server. If this inquiry fails, the
	 * {@link ActionInterface} fails for the same reason. If this {@link Inquiry}
	 * succeeds, so does the {@link ActionInterface} with the same result.
	 * 
	 * @param <R>     The type of the result of the {@link Inquiry}.
	 * @param inquiry The {@link Inquiry} to make.
	 * @return The new {@link ActionInterface} representing the inquiry.
	 */
	<R> ActionInterface<R> action(Inquiry<? extends R> inquiry);

	/**
	 * <p>
	 * Produces a new, dummy {@link ActionInterface} that has the specified result.
	 * New {@link ActionInterface}s can be chained off of the returned
	 * {@link ActionInterface} (all chaining methods work). The returned
	 * {@link ActionInterface} may have already completed with the value specified,
	 * although implementations can choose to determine whether this
	 * {@link ActionInterface} is returned having been completed, or if it must be
	 * queued in the {@link RequestSubsystemInterface} (and, thus, "executed" by a
	 * thread). This method does not fail (except for VM runtime errors), and the
	 * {@link ActionInterface} itself should never complete exceptionally (also
	 * except for VM runtime errors).
	 * </p>
	 * <p>
	 * The default implementation of this {@link ActionInterface} immediately
	 * completes (but still must be queued). It is recommended that implementations
	 * override this method to return an already completed {@link ActionInterface}.
	 * </p>
	 * 
	 * @param <R>   The type of the value that this {@link ActionInterface}
	 *              completed with.
	 * @param value The value that this {@link ActionInterface} completed with.
	 * @return The new {@link ActionInterface}.
	 */
	default <R> ActionInterface<R> completed(R value) {
		return executable(a -> value);
	}

	/**
	 * Returns an {@link ActionInterface} that executes the specified
	 * {@link Supplier}. If such execution completes exceptionally, then the
	 * returned {@link ActionInterface} completes exceptionally for the same reason.
	 * If the {@link Supplier} completes normally, the returned
	 * {@link ActionInterface} does as well with the same result.
	 * 
	 * @param <R>        An upper-bound of the type of the result of the
	 *                   {@link ActionInterface} returned.
	 * @param executable The executable {@link Supplier} that the
	 *                   {@link ActionInterface} should run.
	 * @return The new {@link ActionInterface}.
	 */
	default <R> ActionInterface<R> executable(Supplier<? extends R> executable) {
		return executable(a -> executable.get());
	}

	interface ArlithFunction<R> {
		R execute(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException;
	}

	/**
	 * Returns an {@link ActionInterface} that executes the specified
	 * {@link ArlithFunction}. The {@link ArlithFunction} is provided a
	 * {@link CommunicationConnection} hooked up to the server, through which it can
	 * communicate. If execution of the {@link ArlithFunction} completes exceptionally,
	 * then the returned {@link ActionInterface} completes exceptionally for the
	 * same reason. Otherwise, if the {@link Function} completes normally, the
	 * returned {@link ActionInterface} does as well with the same result.
	 * 
	 * @param <R>        An upper bound on the type of the return value of the
	 *                   {@link ActionInterface}.
	 * @param executable The {@link ArlithFunction} executable to invoke.
	 * @return The new {@link ActionInterface}.
	 */
	<R> ActionInterface<R> executable(ArlithFunction<? extends R> executable);

	void start();

	void stop();
}
