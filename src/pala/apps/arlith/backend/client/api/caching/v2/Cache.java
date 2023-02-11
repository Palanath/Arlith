package pala.apps.arlith.backend.client.api.caching.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.types.TextValue;

/**
 * <p>
 * A class that represents an indefinite client cache of a property. The
 * {@link Cache} can be populated manually at any time by calling code, but will
 * only ever query from the server once (if a retrieval attempt is made while
 * the {@link Cache} is empty).
 * </p>
 * <p>
 * In a nutshell, a {@link Cache} is meant to act as a convenience utility that
 * holds a <i>value</i> for the {@link ArlithClient}, (much like a class field
 * or other variable), but only actually queries the server for the value the
 * first time calling code attempts to read the value from the {@link Cache}.
 * The {@link Cache} is said to be <i>populated</i> when it holds a value, and
 * said to be <i>empty</i> when it does not hold a value. Attempts by calling
 * code to read the value in the {@link Cache} while it is empty will
 * automatically cause the cache to attempt to retrieve the value from the
 * server (in a way that is specified when the {@link Cache} is constructed).
 * {@link Cache}s can be seen as <i>lazily-loaded variables</i>.
 * <p>
 * This class is designed for properties that only need to be <i>queried</i>
 * once, but may be updated later through an event, or possibly a user action.
 * Properties that are suitable for this class incude <i>user statuses</i> (both
 * the logged in user's status and other users' statuses).
 * </p>
 * <p>
 * This class is designed to allow calling code to avoid having to check the
 * case that a cached value is not yet retrieved, and then to resultingly
 * retrieve it, before performing a desired operation with the cached value.
 * </p>
 * <p>
 * The class is designed so that the logic to query the value of the property
 * from the server is provided upon construction. This class provides facilities
 * to <i>get</i> the value from the cache which automatically query the value
 * from the server if needed. The <i>get</i> facilities mirror the request API's
 * two means of sending requests:
 * </p>
 * <ol>
 * <li>The ability to make a request on the current thread, and</li>
 * <li>The ability to queue a request to be made on a dedicated request thread,
 * and have a {@link Consumer} which handles the result (or error) be run
 * after.</li>
 * </ol>
 * <p>
 * This class also provides facilities to allow callers to update the value held
 * by the cache at any time. This is typically used to allow calling code to
 * update the value of the {@link Cache} in if it changes.
 * </p>
 * 
 * @author Palanath
 *
 * @param <V> The type of value held by the {@link Cache}.
 */
public class Cache<V> {

	private interface Waiter {
		/**
		 * <p>
		 * Called upon failure or success of the thread/operation that was already
		 * trying to populate the {@link Cache}. This method is called with a lock over
		 * the {@link Cache} itself, so care should be taken not to perform blocking or
		 * long-standing operations while this method is running, however, it is
		 * guaranteed that the states of the {@link Cache} and the
		 * {@link CachePopulator} will not be modified while this method is running, and
		 * that threads attempting to read such states will block until this method
		 * completes, (unless such modification is done by this method). This method can
		 * safely read the states of all of the {@link Cache} and its populator.
		 * </p>
		 */
		void awaken();
	}

	/**
	 * <p>
	 * This class encapsulates a query that will be made to the server when the
	 * {@link Cache} needs to be populated. It is used to allow the {@link Cache} to
	 * choose between running the query <i>on the calling thread</i> as well as
	 * <i>on the {@link RequestQueue}'s query thread</i>, as well as to let the
	 * {@link Cache} handle multiple calls to the {@link Cache}'s get methods.
	 * </p>
	 * <p>
	 * This class implements {@link Consumer}, and instances of this class are used
	 * to actually populate the surrounding {@link Cache}.
	 * </p>
	 * <p>
	 * 
	 * @author Palanath
	 *
	 */
	private class CachePopulator<T> {

		/**
		 * <p>
		 * Determines whether this query has been started yet. This property is checked
		 * and set by one of the {@link Cache}'s three different {@link Cache#get()
		 * getter methods}.
		 * </p>
		 * <p>
		 * If the value of this variable is checked while synchronized over the
		 * {@link Cache}, it will exactly reflect whether this {@link CachePopulator}
		 * has been started. It is used to determine if there is a need to attempt to
		 * query the server to populate the cache, or if such is already underway by
		 * another thread.
		 * </p>
		 */
		private volatile boolean started;
		/**
		 * The {@link RequestQueue} upon which the request will be made.
		 */
		private final RequestQueue requestQueue;
		/**
		 * A {@link Supplier} that gives the {@link Inquiry} once the actual
		 * cache-population needs to occur. This is done so that the {@link Inquiry} and
		 * its parameters do not have to be known at construction time of the
		 * {@link Cache} (since that is when this {@link CachePopulator} is built).
		 */
		private final Supplier<Inquiry<? extends T>> inquirySupplier;
		/**
		 * A {@link Function} used to convert the result of the {@link Inquiry} made to
		 * the server to the type that this {@link Cache} wishes to store. This is often
		 * used to do things like convert the result of an {@link Inquiry} that is e.g.
		 * a {@link TextValue}, to a {@link String}. This {@link Function} is often very
		 * trivial (and frequently a method reference).
		 */
		private final Function<? super T, ? extends V> resultConverter;

		public CachePopulator(RequestQueue requestQueue, Supplier<Inquiry<? extends T>> inquirySupplier,
				Function<? super T, ? extends V> resultConverter) {
			this.requestQueue = requestQueue;
			this.inquirySupplier = inquirySupplier;
			this.resultConverter = resultConverter;
		}

		private final List<Waiter> waiters = new ArrayList<>(0);

		public V run() throws CommunicationProtocolError {
			return resultConverter.apply(requestQueue.inquire(inquirySupplier.get()));
		}

	}

	/**
	 * The value currently in the {@link Cache}, or <code>null</code> if there is no
	 * value in the {@link Cache} yet. (Note that the value held by a populated
	 * {@link Cache} may be <code>null</code>, so this property cannot always be
	 * used to check if the {@link Cache} is already populated. For such,
	 * {@link #isCached()} can be used, which checks if {@link #query} is
	 * <code>null</code>.)
	 */
	private V value;

	/**
	 * The operation that queries the result if it is requested but is not already
	 * in the {@link Cache}. This is set to <code>null</code> once the result is
	 * requested to indicate that the result has been requested. It is <i>not</i>
	 * set to <code>null</code> if requesting the result fails.
	 */
	private volatile CachePopulator<?> query;

	/**
	 * <p>
	 * Updates the {@link Cache} to hold the specified value. If the {@link Cache}
	 * previously was empty, this method changes its state. If the {@link Cache}
	 * previously was non-empty, this method updates it so that the value it holds
	 * is the one specified.
	 * </p>
	 * <p>
	 * After a call to this method, the {@link Cache} will never attempt to query
	 * from the server.
	 * </p>
	 * 
	 * @param item The item to populate the {@link Cache} with.
	 */
	public void updateItem(V item) {
		this.value = item;
		query = null;
	}

	/**
	 * Returns whether this {@link Cache} is <i>populated</i>. If this method is
	 * <code>false</code> then the next attempt to {@link #get()} the value (using
	 * one of the three retrieval methods, also including {@link #get(Consumer)} and
	 * {@link #get(Consumer, Consumer)}), will cause the value to be retrieved from
	 * the server. Otherwise, the value is supplied immediately and no query to the
	 * server is made.
	 * 
	 * @return <code>true</code> if the {@link Cache} is populated,
	 *         <code>false</code> if it is empty.
	 */
	public boolean isPopulated() {
		return query == null;
	}

	/**
	 * Gets the value from this {@link Cache}. If the {@link Cache} is empty, this
	 * method queries the value from the server on the calling thread before
	 * returning it.
	 * 
	 * @return The value.
	 * @throws CommunicationProtocolError If the {@link Inquiry} to being made
	 *                                    results in a
	 *                                    {@link CommunicationProtocolError}. This
	 *                                    exception only occurs when this method is
	 *                                    called while the cache is not populated
	 *                                    and an attempt to populate it results in
	 *                                    an error. The only
	 *                                    {@link CommunicationProtocolError} that
	 *                                    can be thrown by this method are those
	 *                                    that are made by the {@link Inquiry}, so
	 *                                    this method may be overridden by subtypes
	 *                                    to reify the types of exceptions thrown by
	 *                                    this method.
	 */
	public V get() throws CommunicationProtocolError {
		// Check for current status.
		while (true)
			synchronized (this) {
				if (isPopulated())
					return value;
				else if (isRequesting()) {
					query.waiters.add(this::notify);// Simply notifying does not guarantee order!
					// Ideally, though, that notify call would notify THIS thread, since THIS waiter
					// was called.
					// TODO Fix this.
					try {
						wait();
					} catch (InterruptedException e) {
						throw new RuntimeException("Interrupted", e);
					}
				} else {
					query.started = true;
					break;
				}
			}

		// No thread was requesting and the cache was not populated; make the request.
		V v;
		try {
			v = value = query.run();
		} catch (Throwable e) {
			// If an error occurs, the next object should be given the chance to make its
			// query.
			synchronized (this) {
				query.started = false;
				if (!query.waiters.isEmpty())
					query.waiters.remove(0).awaken();
			}
			throw e;
		}
		synchronized (this) {
			List<Waiter> waiters = query.waiters;
			query = null;
			for (Waiter w : waiters)
				w.awaken();
		}
		return v;
	}

	private boolean isRequesting() {
		return !isPopulated() && query.started;
	}

	/**
	 * <p>
	 * Gets the value from this {@link Cache} and supplies the specified
	 * {@link Consumer} with it.
	 * </p>
	 * <ul>
	 * <li>If the {@link Cache} is already populated, the provided {@link Consumer}
	 * is called on <b>this</b> {@link Thread} with the value in the {@link Cache}.
	 * Otherwise,</li>
	 * <li>If the cache is empty, this method queues a request to the server that
	 * makes the {@link Inquiry}, populates this {@link Cache}, and then runs the
	 * provided {@link Consumer} <code>resultHandler</code> with the result, all of
	 * which happens on the {@link RequestQueue}'s dedicated thread.</li>
	 * </ul>
	 * <p>
	 * Care should usually be taken to assure that, in either case, the provided
	 * result handler is not a blocking or intensive task. The purpose of this
	 * method is to offload the operation of making the server {@link Inquiry} (and
	 * populating this {@link Cache} with the response) onto a separate thread; the
	 * provided {@link Consumer} may run on the calling thread.
	 * </p>
	 * 
	 * @param resultHandler The {@link Consumer} that will receive the value.
	 */
	public void get(Consumer<? super V> resultHandler) {
		get(resultHandler, null);
	}

	public void get(Consumer<? super V> resultHandler, Consumer<? super Throwable> errorHandler) {
		// This is pulled out (and called below) to reduce code redundancy; the logic
		// for what this should do when awoken after waiting is the same as what it
		// should do the first time this method is called.
		Waiter waiter = new Waiter() {
			@SuppressWarnings("unchecked")
			@Override
			public void awaken() {
				// Called in synchronized(this) block
				if (isPopulated())
					try {
						resultHandler.accept(value);
					} catch (Exception e) {
						e.printStackTrace();// Ignore exceptions propagated by resultHandler.
					}
				else if (isRequesting())
					query.waiters.add(this);// (Re-)add to list of waiters.
				else {
					query.started = true;
					Inquiry<?> i;
					try {
						i = query.inquirySupplier.get();
					} catch (Exception e) {
						try {
							errorHandler.accept(e);
						} finally {
							query.started = false;
							if (!query.waiters.isEmpty())
								query.waiters.remove(0).awaken();
						}
						return;
					}
					query.requestQueue.queue(i, t -> {

						try {
							value = ((Function<Object, V>) query.resultConverter).apply(t);
						} catch (Exception e) {
							try {
								errorHandler.accept(e);
							} finally {
								synchronized (Cache.this) {
									query.started = false;
									if (!query.waiters.isEmpty())
										query.waiters.remove(0).awaken();
								}
							}
							return;
						}

						synchronized (Cache.this) {
							List<Waiter> waiters = query.waiters;
							query = null;
							for (Waiter w : waiters)
								w.awaken();
						}

					}, t -> {
						try {
							errorHandler.accept(t);
						} finally {
							synchronized (Cache.this) {
								query.started = false;
								if (!query.waiters.isEmpty())
									query.waiters.remove(0).awaken();
							}
						}
					});

				}
			}
		};
		synchronized (this) {
			waiter.awaken();
		}
	}

	/**
	 * Constructs this {@link Cache} already initialized with the provided value. If
	 * the {@link Cache} is constructed in this way, the value is never queried from
	 * the server after construction; it is always just returned. The value may
	 * still be updated however using {@link #updateItem(Object)} after
	 * construction.
	 * 
	 * @param value The value to construct the {@link Cache} with.
	 */
	public Cache(V value) {
		this.value = value;
	}

	public Cache(Inquiry<? extends V> inquiry, RequestQueue requestQueue) {
		query = new CachePopulator<>(requestQueue, () -> inquiry, a -> a);
	}

}
