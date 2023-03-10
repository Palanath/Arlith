package pala.apps.arlith.backend.client.api.caching;

import java.util.function.Supplier;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.client.requests.v2.RequestSubsystem;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

/**
 * <p>
 * Represents a cache of a value that the {@link ArlithClient} uses. This
 * cache is designed to facilitate storing data that has already been received
 * from the server and returning it again on subsequent query attempts, even in
 * multithreaded contexts.
 * </p>
 * <p>
 * Caches begin in an <i>unpopulated</i> state. Unpopulated caches have not yet
 * cached anything; they are empty. A cache becomes {@link #populated} when
 * either
 * <ol>
 * <li>Calling code invokes a {@link #get()} action for the cache for the first
 * time, or</li>
 * <li>The cache is populated manually by a call to
 * {@link #populate(Object)}.</li>
 * </ol>
 * 
 * @author Palanath
 *
 * @param <O> The type of the value held in this {@link ClientCache}.
 */
public abstract class ClientCache<O> extends Cache<O> {

	public static interface Querier<O> {
		O query(CommunicationConnection conn) throws CommunicationProtocolError, RuntimeException;
	}

	public static final class ClientCacheMaker<O> extends ClientCache<O> {

		private final Querier<? extends O> querier;

		@Override
		protected O queryFromServer(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException {
			return querier.query(connection);
		}

		public ClientCacheMaker(RequestSubsystem reqsys, Querier<? extends O> querier) {
			super(reqsys);
			this.querier = querier;
		}

		public ClientCacheMaker(Supplier<? extends RequestSubsystem> reqsys, Querier<? extends O> querier) {
			super(reqsys);
			this.querier = querier;
		}

	}

	/**
	 * Constructs a new {@link ClientCache} that makes {@link #get()}
	 * {@link ActionInterface}s on the provided {@link RequestSubsystem}.
	 * 
	 * @param reqsys The request system to use.
	 */
	public ClientCache(RequestSubsystem reqsys) {
		super(() -> reqsys);
	}

	public ClientCache() {
	}

	/**
	 * <p>
	 * Constructs a new {@link ClientCache} that makes {@link #get()}
	 * {@link ActionInterface}s based on the {@link RequestSubsystem}
	 * returned by the provided {@link Supplier}. The {@link Supplier} is called
	 * every time the cache needs to make a new {@link ActionInterface}.
	 * </p>
	 * <p>
	 * This constructor is primarily available just so that the client can construct
	 * new {@link ClientCache}s in initialization blocks before setting the final
	 * {@link RequestSubsystem} in its own constructor.
	 * </p>
	 * 
	 * @param reqsys A {@link Supplier} that should provide the
	 *               {@link RequestSubsystem} whenever {@link #get()} needs
	 *               to be called.
	 */
	public ClientCache(Supplier<? extends RequestSubsystem> reqsys) {
		super(reqsys);
	}

	/**
	 * <p>
	 * Queries the value that this cache holds from the server.
	 * </p>
	 * <p>
	 * This method is called when caller code attempts to {@link #get()} the value
	 * of this cache, but this cache is empty; (i.e. the value requested by the
	 * {@link #get()} action invocation has not been received from the server yet).
	 * </p>
	 * <p>
	 * This method is tasked with sending a request (or requests) and performing
	 * whatever other operations necessary to retrieve the value from the server.
	 * </p>
	 * 
	 * @param connection The {@link CommunicationConnection} to contact the server over
	 *                   to retrieve the value, to be used if needed.
	 * @return The value retrieved from the server, to be stored in the cache.
	 * @throws CommunicationProtocolError         In case there's a {@link CommunicationProtocolError} while
	 *                          communicating with the server.
	 * @throws RuntimeException In case there's some other exception that occurs
	 *                          during the query.
	 */
	protected abstract O queryFromServer(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException;

	@Override
	protected void populate(CommunicationConnection connection) throws CommunicationProtocolError, RuntimeException {
		value = queryFromServer(connection);
		populated = true;
	}

}
