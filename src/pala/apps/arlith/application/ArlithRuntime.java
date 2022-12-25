package pala.apps.arlith.application;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import pala.apps.arlith.Arlith;
import pala.apps.arlith.graphics.windows.ArlithWindow;

public class ArlithRuntime {
	public enum Instance {
		SERVER, CLIENT
	}

	public static ArlithWindow window;

	private static final WeakHashMap<Thread, Instance> THREADS = new WeakHashMap<>();
	/**
	 * A map of arbitrary data linked to a thread. This can be used by developers
	 * for any purpose, and finds use in linking client and server threads to
	 * information related to their purpose for logging.
	 */
	private static final WeakHashMap<Thread, Map<Object, Object>> THREAD_DATA = new WeakHashMap<>();

	/**
	 * <p>
	 * Gets the thread data {@link Map} for the specified thread. If no map exists,
	 * it is created. The map can store any arbitrary developer data and can be
	 * retrieved at any time with a reference to the {@link Thread}. It is
	 * recommended not to invoke {@link Map#clear()} on the {@link Map} or for one
	 * system or component to modify values set by another system or component.
	 * </p>
	 * <p>
	 * Threads do not have to be {@link #register(Instance) registered} for this
	 * method to fully work.
	 * </p>
	 * 
	 * @param thread The {@link Thread} to obtain the data map of.
	 * @return The data map for the {@link Thread}.
	 */
	public static Map<Object, Object> getThreadData(Thread thread) {
		Map<Object, Object> data = THREAD_DATA.get(thread);
		if (data == null)
			THREAD_DATA.put(thread, data = new HashMap<>());
		return data;
	}

	/**
	 * Gets the thread data {@link Map} for the current thread. See
	 * {@link #getThreadData(Thread)} for more info.
	 * 
	 * @return {@link #getThreadData(Thread)} invoked with the result of a call to
	 *         {@link Thread#currentThread()} as the only argument.
	 */
	public static Map<Object, Object> getThreadData() {
		return getThreadData(Thread.currentThread());
	}

	public static Thread newThread(Instance instance) {
		Thread t = new Thread();
		register(instance, t);
		return t;
	}

	public static Thread newThread(Instance instance, Runnable runnable) {
		Thread t = new Thread(runnable);
		register(instance, t);
		return t;
	}

	public static void register(Instance instance) {
		register(instance, Thread.currentThread());
	}

	/**
	 * Registers the provided {@link Thread} with this class's global thread
	 * tracking system. This method also applies the client/server's error handler,
	 * as appropriate.
	 * 
	 * @param instance Whether this is a {@link Instance#CLIENT client} or
	 *                 {@link Instance#SERVER server} thread.
	 * @param thread   The {@link Thread} to register.
	 */
	public static void register(Instance instance, Thread thread) {
		THREADS.put(thread, instance);
		switch (instance) {
		case CLIENT:
			thread.setUncaughtExceptionHandler((t, e) -> {
				Arlith.getLogger().err("A [CLIENT] thread, " + t.getName() + " had an uncaught error.");
				Arlith.getLogger().err(e);
			});
			break;
		case SERVER:
			thread.setUncaughtExceptionHandler((t, e) -> {
				Arlith.getLogger().err("A [SERVER] thread, " + t.getName() + " had an uncaught error.");
				Arlith.getLogger().err(e);
			});
		}
	}
}
