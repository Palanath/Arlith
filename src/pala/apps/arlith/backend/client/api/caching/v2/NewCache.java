package pala.apps.arlith.backend.client.api.caching.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.client.requests.v3.RequestQueue;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.libs.generic.util.Box;

/**
 * <p>
 * A class that represents an indefinite client cache of a property. The
 * {@link NewCache} can be populated manually at any time by calling code, but
 * will only ever query from the server, at max, once (if a retrieval attempt is
 * made while the {@link NewCache} is empty). This class supports the
 * <code>null</code> value.
 * </p>
 * <p>
 * In a nutshell, a {@link NewCache} is meant to act as a convenience utility
 * that holds a <i>value</i> for the {@link ArlithClient}, (much like a class
 * field or other variable), but only actually queries the server for the value
 * the first time calling code attempts to read the value from the
 * {@link NewCache}. The {@link NewCache} is said to be <i>populated</i> when it
 * holds a value, and said to be <i>empty</i> when it does not hold a value.
 * Attempts by calling code to read the value in the {@link NewCache} while it
 * is empty will automatically cause the cache to attempt to retrieve the value
 * from the server (in a way that is specified when the {@link NewCache} is
 * constructed). {@link NewCache}s can be seen as <i>lazily-loaded
 * variables</i>.
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
 * update the value of the {@link NewCache} in if it changes.
 * </p>
 * 
 * @author Palanath
 *
 * @param <V> The type of value held by the {@link NewCache}.
 */
public class NewCache<V> {// Temporarily rename to NewCache until all references to old Cache are gone.

	/**
	 * A task that is waiting on the completion of the currently operating attempt
	 * to populate this {@link NewCache}.
	 * 
	 * @author Palanath
	 *
	 */
	private interface Waiter {
		/**
		 * <p>
		 * Called upon failure or success of the thread/operation that was already
		 * trying to populate the {@link NewCache}. This method is called with a lock
		 * over the {@link NewCache} itself, so care should be taken not to perform
		 * blocking or long-standing operations while this method is running, however,
		 * it is guaranteed that the states of the {@link NewCache} and the
		 * {@link CachePopulator} will not be modified while this method is running, and
		 * that threads attempting to read such states will block until this method
		 * completes, (unless such modification is done by this method). This method can
		 * safely read the states of all of the {@link NewCache} and its populator.
		 * </p>
		 */
		void awaken();
	}

	/**
	 * <p>
	 * This class encapsulates a query that will be made to the server when the
	 * {@link NewCache} needs to be populated. It is used to allow the
	 * {@link NewCache} to choose between running the query <i>on the calling
	 * thread</i> as well as <i>on the {@link RequestQueue}'s query thread</i>, as
	 * well as to let the {@link NewCache} handle multiple calls to the
	 * {@link NewCache}'s get methods.
	 * </p>
	 * <p>
	 * This class implements {@link Consumer}, and instances of this class are used
	 * to actually populate the surrounding {@link NewCache}.
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
		 * and set by one of the {@link NewCache}'s three different
		 * {@link NewCache#get() getter methods}.
		 * </p>
		 * <p>
		 * If the value of this variable is checked while synchronized over the
		 * {@link NewCache}, it will exactly reflect whether this {@link CachePopulator}
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
		 * {@link NewCache} (since that is when this {@link CachePopulator} is built).
		 */
		private final Supplier<? extends Inquiry<? extends T>> inquirySupplier;
		/**
		 * A {@link Function} used to convert the result of the {@link Inquiry} made to
		 * the server to the type that this {@link NewCache} wishes to store. This is
		 * often used to do things like convert the result of an {@link Inquiry} that is
		 * e.g. a {@link TextValue}, to a {@link String}. This {@link Function} is often
		 * very trivial (and frequently a method reference).
		 */
		private final Function<? super T, ? extends V> resultConverter;

		public CachePopulator(RequestQueue requestQueue, Supplier<? extends Inquiry<? extends T>> inquirySupplier,
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
	 * The value currently in the {@link NewCache}, or <code>null</code> if there is
	 * no value in the {@link NewCache} yet. (Note that the value held by a
	 * populated {@link NewCache} may be <code>null</code>, so this property cannot
	 * always be used to check if the {@link NewCache} is already populated. For
	 * such, {@link #isCached()} can be used, which checks if {@link #query} is
	 * <code>null</code>.)
	 */
	private V value;

	/**
	 * The operation that queries the result if it is requested but is not already
	 * in the {@link NewCache}. This is set to <code>null</code> once the result is
	 * requested to indicate that the result has been requested. It is <i>not</i>
	 * set to <code>null</code> if requesting the result fails.
	 */
	private volatile CachePopulator<?> query;

	/**
	 * <p>
	 * Updates the {@link NewCache} to hold the specified value. If the cache was
	 * previously empty, it becomes populated with the specified value. If it was
	 * not empty, its value is replaced with the one specified.
	 * </p>
	 * <p>
	 * This method is exposed with the intent to allow the client to update the
	 * cache once the value it represents changes on the server and the server sends
	 * a notification (event) to the client. This method should be called whenever
	 * the cache's value is changed (either from being populated or from subsequent
	 * update). {@link NewCache}'s logic calls this method any time the value in the
	 * cache is changed.
	 * </p>
	 * <p>
	 * After a call to this method, the {@link NewCache} will never attempt to query
	 * from the server again.
	 * </p>
	 * 
	 * @param item The item to populate the {@link NewCache} with.
	 */
	public synchronized void updateItem(V item) {
		this.value = item;
		query = null;
	}

	/**
	 * Returns whether this {@link NewCache} is <i>populated</i>. If this method is
	 * <code>false</code> then the next attempt to {@link #get()} the value (using
	 * one of the three retrieval methods, also including {@link #queue(Consumer)}
	 * and {@link #queue(Consumer, Consumer)}), will cause the value to be retrieved
	 * from the server. Otherwise, the value is supplied immediately and no query to
	 * the server is made.
	 * 
	 * @return <code>true</code> if the {@link NewCache} is populated,
	 *         <code>false</code> if it is empty.
	 */
	public boolean isPopulated() {
		return query == null;
	}

	/**
	 * Checks if this cache {@link #isPopulated() is populated}, and, if it is,
	 * returns the value from the cache. Otherwise, returns <code>null</code>. This
	 * method does not throw {@link CommunicationProtocolError}s because it never
	 * causes the cache to populate.
	 * 
	 * @return The value in the cache, or <code>null</code> if the cache is not
	 *         populated yet.
	 */
	public V getIfPopulated() {
		return value;
	}

	/**
	 * If this cache {@link #isPopulated() is populated}, this method passes the
	 * value in the cache to the specified {@link Consumer}. Otherwise, this method
	 * does nothing.
	 * 
	 * @param action The {@link Consumer} to receive the value in the cache, if
	 *               populated.
	 */
	public void doIfPopulated(Consumer<? super V> action) {
		if (isPopulated())
			action.accept(value);
	}

	/**
	 * Gets the value from this {@link NewCache}. If the {@link NewCache} is empty,
	 * this method queries the value from the server on the calling thread before
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
	 *                                    this method. Note that this exception will
	 *                                    never be thrown if after a call to
	 *                                    {@link #isPopulated()} returns
	 *                                    <code>true</code>, as, once this
	 *                                    {@link NewCache} is populated, it will
	 *                                    never attempt to repopulate itself.
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
			updateItem(v = query.run());
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
			for (Waiter w : waiters)
				w.awaken();
		}
		return v;
	}

	private boolean isRequesting() {
		return !isPopulated() && query.started;
	}

	public void queue() {
		queue(null);
	}

	/**
	 * <p>
	 * Gets the value from this {@link NewCache} and supplies the specified
	 * {@link Consumer} with it.
	 * </p>
	 * <ul>
	 * <li>If the {@link NewCache} is already populated, the provided
	 * {@link Consumer} is called on <b>this</b> {@link Thread} with the value in
	 * the {@link NewCache}. Otherwise,</li>
	 * <li>If the cache is empty, this method queues a request to the server that
	 * makes the {@link Inquiry}, populates this {@link NewCache}, and then runs the
	 * provided {@link Consumer} <code>resultHandler</code> with the result, all of
	 * which happens on the {@link RequestQueue}'s dedicated thread.</li>
	 * </ul>
	 * <p>
	 * Care should usually be taken to assure that, in either case, the provided
	 * result handler is not a blocking or intensive task. The purpose of this
	 * method is to offload the operation of making the server {@link Inquiry} (and
	 * populating this {@link NewCache} with the response) onto a separate thread;
	 * the provided {@link Consumer} may run on the calling thread.
	 * </p>
	 * 
	 * @param resultHandler The {@link Consumer} that will receive the value.
	 */
	public void queue(Consumer<? super V> resultHandler) {
		queue(resultHandler, null);
	}

	public void queue(Consumer<? super V> resultHandler, Consumer<? super Throwable> errorHandler) {
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
						if (resultHandler != null)
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
							if (errorHandler != null)
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
							updateItem(((Function<Object, V>) query.resultConverter).apply(t));
						} catch (Exception e) {
							try {
								if (errorHandler != null)
									errorHandler.accept(e);
							} finally {
								synchronized (NewCache.this) {
									query.started = false;
									if (!query.waiters.isEmpty())
										query.waiters.remove(0).awaken();
								}
							}
							return;
						}

						List<Waiter> waiters;
						synchronized (NewCache.this) {
							waiters = query.waiters;
						}
						try {
							resultHandler.accept(value);
						} finally {
							synchronized (NewCache.this) {
								for (Waiter w : waiters)
									w.awaken();
							}
						}

					}, t -> {
						try {
							if (errorHandler != null)
								errorHandler.accept(t);
						} finally {
							synchronized (NewCache.this) {
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

	public CompletableFuture<V> future() {
		Box<Waiter> w = new Box<>();
		CompletableFuture<V> f = new CompletableFuture<V>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				synchronized (NewCache.this) {
					return query != null && query.waiters.remove(w.value) && super.cancel(mayInterruptIfRunning);
				}
			}
		};
		w.value = new Waiter() {
			@SuppressWarnings("unchecked")
			@Override
			public void awaken() {
				if (isPopulated())
					f.complete(value);
				else if (isRequesting()) {
					query.waiters.add(this);
				} else {
					query.started = true;
					Inquiry<?> inquiry;
					try {
						inquiry = query.inquirySupplier.get();
					} catch (Exception e) {
						f.completeExceptionally(e);
						if (!query.waiters.isEmpty())
							query.waiters.remove(0).awaken();
						return;
					}

					query.requestQueue.queue(inquiry, a -> {
						try {
							updateItem(((Function<Object, V>) query.resultConverter).apply(a));
						} catch (Exception e) {
							f.completeExceptionally(e);
							synchronized (NewCache.this) {
								if (!query.waiters.isEmpty())
									query.waiters.remove(0).awaken();
							}
							return;
						}

						List<Waiter> waiters;
						synchronized (NewCache.this) {
							waiters = query.waiters;
						}

						try {
							f.complete(value);
						} finally {
							synchronized (NewCache.this) {
								for (Waiter w : waiters)
									w.awaken();
							}
						}
					}, a -> {
						try {
							f.completeExceptionally(a);
						} catch (Exception e) {
							synchronized (NewCache.this) {
								if (!query.waiters.isEmpty())
									query.waiters.remove(0).awaken();
							}
						}
					});
				}
			}
		};
		synchronized (this) {
			w.value.awaken();
		}
		return f;
	}

	/**
	 * Constructs this {@link NewCache} already initialized with the provided value.
	 * If the {@link NewCache} is constructed in this way, the value is never
	 * queried from the server after construction; it is always just returned. The
	 * value may still be updated however using {@link #updateItem(Object)} after
	 * construction.
	 * 
	 * @param value The value to construct the {@link NewCache} with.
	 */
	public NewCache(V value) {
		updateItem(value);
	}

	/**
	 * <p>
	 * Creates a new, empty {@link NewCache} that makes the provided {@link Inquiry}
	 * to the server. The {@link Inquiry} should return a value of the type stored
	 * in this {@link NewCache}, and the value is always directly placed into this
	 * {@link NewCache} when the {@link Inquiry} is made. The specified
	 * {@link RequestQueue} is used to make the request.
	 * </p>
	 * <p>
	 * This constructor is equivalent to calling
	 * {@link NewCache#Cache(Supplier, RequestQueue)} with
	 * <code>() -> inquiry</code> as the first argument, i.e., a dummy
	 * {@link Supplier} that always returns the specified {@link Inquiry} is used.
	 * Additionally, this constructor is equivalent to
	 * {@link NewCache#Cache(Supplier, Function, RequestQueue)}, in that the same
	 * dummy supplier is used and the conversion {@link Function} is
	 * <code>a -> (V) a</code>, or <code>null</code> (such a {@link Function} and
	 * <code>null</code> are treated by that constructor as equivalent).
	 * </p>
	 * 
	 * @param inquiry      The {@link Inquiry} that gets made to the server when the
	 *                     {@link NewCache} needs to be populated.
	 * @param requestQueue The {@link RequestQueue} to make the {@link Inquiry} on.
	 */
	public NewCache(Inquiry<? extends V> inquiry, RequestQueue requestQueue) {
		this(() -> inquiry, requestQueue);
	}

	/**
	 * <p>
	 * Creates a new {@link NewCache} with the specified {@link Inquiry} to be used
	 * when populating, the specified {@link Function} to convert the result to an
	 * appropriate value, and the specified {@link RequestQueue} to make the
	 * {@link Inquiry} on. The <code>resultConverter</code> function should
	 * generally be lightweight, as it is not specified what {@link Thread} will
	 * actually execute it (it may get executed on the {@link RequestQueue}'s
	 * backing thread or on some thread that calls {@link #get()}).
	 * </p>
	 * <p>
	 * Exceptions occurring on the provided <code>resultConverter</code>
	 * {@link Function} will be propagated to the <code>errorHandler</code> provided
	 * in calls to {@link #queue(Consumer, Consumer)}, or to the caller if calling
	 * {@link #get()}.
	 * </p>
	 * 
	 * @param <T>             The type of the result of the {@link Inquiry}.
	 * @param inquiry         The {@link Inquiry} that will be made to the server.
	 * @param resultConverter A {@link Function} to convert the result of the
	 *                        {@link Inquiry} to the type that this {@link NewCache}
	 *                        should store.
	 * @param requestQueue    The {@link RequestQueue} to make the {@link Inquiry}
	 *                        on.
	 */
	public <T> NewCache(Inquiry<? extends T> inquiry, Function<? super T, ? extends V> resultConverter,
			RequestQueue requestQueue) {
		this(() -> inquiry, resultConverter, requestQueue);
	}

	/**
	 * <p>
	 * Constructs a new {@link NewCache} with the specified {@link Inquiry}
	 * {@link Supplier} and the specified {@link RequestQueue}. When the
	 * {@link NewCache} needs to be populated, the {@link Supplier} is queried for a
	 * new {@link Inquiry} to make on the {@link RequestQueue}. Note that the
	 * {@link Supplier} may be queried multiple times if making {@link Inquiry
	 * Inquiries} fails, causing the {@link NewCache} to need to attempt to
	 * repopulate itself again.
	 * </p>
	 * <p>
	 * Exceptions on the {@link Supplier} are passed to the
	 * <code>errorHandler</code> if a call to {@link #queue(Consumer, Consumer)} is
	 * the source of the need to populate the {@link NewCache}, and are relayed to
	 * the caller if {@link #get()} is instead the source.
	 * </p>
	 * 
	 * @param inquirySupplier A {@link Supplier} that provides the {@link Inquiry}
	 *                        when the {@link NewCache} needs to populate itself.
	 * @param requestQueue    The {@link RequestQueue} to make the {@link Inquiry}
	 *                        on when the {@link NewCache} needs to populate itself.
	 */
	public NewCache(Supplier<? extends Inquiry<? extends V>> inquirySupplier, RequestQueue requestQueue) {
		this(inquirySupplier, a -> a, requestQueue);
	}

	/**
	 * <p>
	 * Constructs a new {@link NewCache} with the specified {@link Inquiry}
	 * {@link Supplier}, specified <code>resultConverter</code> {@link Function},
	 * and specified {@link RequestQueue} to make the {@link Inquiry} on.
	 * </p>
	 * <p>
	 * When the {@link NewCache} needs to be populated, the {@link Supplier} is
	 * queried for a new {@link Inquiry} to make on the {@link RequestQueue}, the
	 * {@link Inquiry} is made, and the result is passed to the
	 * <code>resultConverter</code> {@link Function} provided.
	 * </p>
	 * <p>
	 * Note that the {@link Supplier}, and even the <code>resultConverter</code>
	 * {@link Function}, may be queried multiple times if making {@link Inquiry
	 * Inquiries} fails, causing the {@link NewCache} to need to attempt to
	 * repopulate itself again.
	 * </p>
	 * <p>
	 * Both the provided {@link Supplier} and {@link Function} should generally be
	 * lightweight. Exceptions on either are relayed to the caller if {@link #get()}
	 * is invoked and relayed to the <code>errorHandler</code> if
	 * {@link #queue(Consumer, Consumer)} is invoked.
	 * </p>
	 * 
	 * @param <T>             The type of the result of the {@link Inquiry}.
	 * @param inquirySupplier The {@link Supplier} to supply {@link Inquiry
	 *                        Inquiries} that result in <code>T</code> objects.
	 * @param resultConverter The {@link Function} used to convert from the result
	 *                        of the {@link Inquiry} (of type <code>T</code>) to a
	 *                        value suitable for storage in this {@link NewCache}
	 *                        (of type <code>V</code>, may be <code>null</code>).
	 * @param requestQueue    The {@link RequestQueue} to make the {@link Inquiry}
	 *                        on.
	 */
	public <T> NewCache(Supplier<? extends Inquiry<? extends T>> inquirySupplier,
			Function<? super T, ? extends V> resultConverter, RequestQueue requestQueue) {
		query = new CachePopulator<>(requestQueue, inquirySupplier, resultConverter);
	}

}
