package pala.apps.arlith.app.application;

import java.util.WeakHashMap;

import javafx.application.Platform;
import pala.apps.arlith.graphics.windows.ArlithWindow;

public class ArlithRuntime {
	public static ArlithWindow window;
	private static final WeakHashMap<Thread, Instance> threads = new WeakHashMap<>();

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
		threads.put(thread, instance);
		switch (instance) {
		case CLIENT:
			thread.setUncaughtExceptionHandler((t, e) -> {
				System.err.println("The [CLIENT] thread: " + t.getName() + " had an uncaught error:");
				e.printStackTrace();
			});
			break;
		case SERVER:
			thread.setUncaughtExceptionHandler((t, e) -> {
				System.err.println("The [SERVER] thread: " + t.getName() + " had an uncaught error:");
				e.printStackTrace();
			});
		}
	}

	public static void register(Instance instance) {
		register(instance, Thread.currentThread());
	}

	public static Instance instance() {
		return instance(Thread.currentThread());
	}

	public static Instance instance(Thread thread) {
		if (Platform.isFxApplicationThread())
			return Instance.CLIENT;
		else
			return threads.get(thread);
	}

	public enum Instance {
		SERVER, CLIENT
	}
}
