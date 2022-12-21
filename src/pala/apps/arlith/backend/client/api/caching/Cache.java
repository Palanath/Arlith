package pala.apps.arlith.backend.client.api.caching;

import java.util.function.Supplier;

import pala.apps.arlith.backend.client.requests.v2.ActionInterface;
import pala.apps.arlith.backend.client.requests.v2.RequestSubsystemInterface;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;

public abstract class Cache<O> {

	protected O value;
	protected volatile boolean populated;
	protected Supplier<? extends RequestSubsystemInterface> reqsys;

	protected final Supplier<? extends RequestSubsystemInterface> getReqsys() {
		return reqsys;
	}

	public Cache(Supplier<? extends RequestSubsystemInterface> reqsys) {
		this.reqsys = reqsys;
	}

	public Cache() {
	}

	public void setReqsys(Supplier<? extends RequestSubsystemInterface> reqsys) {
		this.reqsys = reqsys;
	}

	public void setReqsys(RequestSubsystemInterface reqsys) {
		setReqsys(() -> reqsys);
	}

	public final O poll() {
		return value;
	}

	/**
	 * Returns <code>true</code>, immediately, if this cache is currently populated.
	 * This method is threadsafe.
	 * 
	 * @return The value of {@link #populated}; <code>true</code> if this cache is
	 *         populated, <code>false</code> otherwise.
	 */
	public boolean isPopulated() {
		return populated;
	}

	/**
	 * <p>
	 * Updates the value held by this cache with the new, provided value. This
	 * method can be called even if the cache is not populated, in which case it is
	 * equivalent to {@link #populate(Object)}.
	 * </p>
	 * <p>
	 * This method is intended to support cases where a client's property needs to
	 * be updated due to reception of an event from the server. Events can update
	 * {@link Cache} values using this method.
	 * </p>
	 * 
	 * @param newValue The new value to put into the cache.
	 */
	public synchronized void update(O newValue) {
		if (!isPopulated())
			populate(newValue);
		else
			value = newValue;
	}

	/**
	 * <p>
	 * Immediately populates this {@link ClientCache} with the provided value. This
	 * method is provided so that caches can be populated by client API code if
	 * their value is received before they're populated <i>and</i> before a request
	 * to retrieve their value (via {@link #get()}'s returned
	 * {@link ActionInterface}) has been made.
	 * </p>
	 * <p>
	 * Such may happen when, for example, the server sends an event containing the
	 * information that this cache would store, or, for example, when the client
	 * requests some <i>other</i> value from the server, and some information sent
	 * in the response contains the value that this cache is intended to hold.
	 * </p>
	 * <ul>
	 * <li>This method marks the cache as populated.</li>
	 * <li>This method may not be called on a populated cache (the cache should be
	 * marked unpopulated first via a call to {@link #clear()}).</li>
	 * <li>This method is synchronized against cache-reads ({@link #get()}), other
	 * cache-sets (other calls to {@link #populate(Object)}), and cache-clears
	 * ({@link #clear()}).
	 * </ul>
	 * <p>
	 * One of the effects of that synchronization is that a query will not be made
	 * to the server "during" the execution of {@link #populate(Object)}; other
	 * threads attempting to {@link #get()} the value of the cache will wait until
	 * this {@link #populate(Object)} call finishes, and then will get the value
	 * that was populated.
	 * </p>
	 * 
	 * @param value The value to populate the cache with.
	 */
	public synchronized void populate(O value) {
		if (populated)
			throw new IllegalStateException("This Cache object is already populated.");
		this.value = value;
		populated = true;
	}

	/**
	 * <p>
	 * Clears this cache by setting {@link #populated} to false and by nulling the
	 * cached {@link #value}. This method can be called whether this cache is
	 * populated or not, and this method sets the cache's state to unpopulated.
	 * Calling this method while the cache is not populated does nothing.
	 * </p>
	 * <p>
	 * This method is synchronized against cache-reads, cache-sets, and other
	 * cache-clears.
	 * </p>
	 * <p>
	 * The <code>null</code>-ing of {@link #value} is done for garbage collection
	 * purposes, although the value of the field {@link #value} does not matter
	 * otherwise if this cache is considered unpopulated.
	 * </p>
	 * 
	 */
	public synchronized void clear() {
		populated = false;
		value = null;
	}

	protected abstract void populate(CommunicationConnection connection)
			throws CommunicationProtocolError, RuntimeException;

	/**
	 * Returns a new {@link ActionInterface} that gets this cache's value. If this
	 * cache is empty, the {@link ActionInterface} will query the server for the
	 * value (and store the value in this cache), which may throw exceptions. If the
	 * cache has a value, the value is simply returned by the
	 * {@link ActionInterface} and no exceptions are thrown.
	 * 
	 * @return A new {@link ActionInterface} representing the operation of reading
	 *         from the cache and querying the server if the cache is empty.
	 */
	public final ActionInterface<O> get() {
		return reqsys.get().executable(a -> {
			// We enter a synchronized block to see if the value has been retrieved from the
			// cache already. If it has, simply return it.
			//
			// If it hasn't we need to request it, INSIDE the synchronized block, so that
			// any other threads that may be running this at the same time do not also try
			// to make a request to the server (they will instead block until the first
			// thread finishes the request, then they will check isPopulated() and see that
			// the cache is populated and just return the contained value).
			synchronized (this) {
				if (!isPopulated())
					populate(a);// If a value has not yet been set to #value, set it.
				return value;// Query the value from the server.
			}
		});
	}

}