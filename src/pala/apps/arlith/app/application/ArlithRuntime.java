package pala.apps.arlith.app.application;

import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.items.LateLoadItem;

public class ArlithRuntime {
	public enum Instance {
		SERVER, CLIENT
	}

	public static ArlithWindow window;

	private static final WeakHashMap<Thread, Instance> threads = new WeakHashMap<>();

	public static final LateLoadItem<Image> MISSING_TEXTURE_IMAGE = new LateLoadItem<>(
			() -> new Image("/pala/apps/arlith/missing-texture.png", false));

	static final Image WINDOW_ICON = new Image("pala/apps/arlith/logo.png");

	public static final Color DEFAULT_BASE_COLOR = Color.GOLD, DEFAULT_ACTIVE_COLOR = Color.RED;

	private static Color baseColor = DEFAULT_BASE_COLOR, activeColor = DEFAULT_ACTIVE_COLOR;

	public static void displayConsole() {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(ArlithRuntime::displayConsole);
		else {
			System.out.println("Console should be shown to user at this point.");
		}
	}

	public static Color getActiveColor() {
		return ArlithRuntime.activeColor;
	}

	public static Color getBaseColor() {
		return baseColor;
	}

	public static Image getWindowIcon() {
		return WINDOW_ICON;
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

	public static void setActiveColor(Color activeColor) {
		ArlithRuntime.activeColor = activeColor;
	}

	public static void setBaseColor(Color baseColor) {
		ArlithRuntime.baseColor = baseColor;
	}
}
