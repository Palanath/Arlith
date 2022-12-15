package pala.apps.arlith.app.logging;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import pala.apps.arlith.app.application.Arlith;

public class Logging {

	private static final Color STANDARD_COLOR = Color.GREEN, WARNING_COLOR = Color.GOLD, ERROR_COLOR = Color.FIREBRICK,
			DEBUG_COLOR = Color.CORNFLOWERBLUE;

	public static void print(String text, Color clr) {
		System.out.println(text);
		// TODO Show color-supporting console.
	}

	private static final BooleanProperty DEBUGGING_ENABLED_PROPERTY = new SimpleBooleanProperty(),
			SHOW_ON_ERROR = new SimpleBooleanProperty(true);

	public static void std(String text) {
		print(text, STANDARD_COLOR);
		System.out.println("[STD]: " + text);
	}

	public static void wrn(String text) {
		print(text, WARNING_COLOR);
		System.out.println("[WARN]: " + text);
	}

	public static void dbg(String text) {
		if (isDebuggingEnabled()) {
			System.out.println("[DEBUG]: " + text);
			print(text, DEBUG_COLOR);
		}
	}

	public static void err(String text) {
		System.out.println("[ERROR]: " + text);
		print(text, ERROR_COLOR);
		Arlith.displayConsole();
	}

	public static void err(Throwable err) {
		err.printStackTrace();
//		if (!Platform.isFxApplicationThread())
//			Platform.runLater(() -> err(err));
//		else {
//			err(err.getMessage());
//			if (isDebuggingEnabled())
//				try (PrintWriter writer = Application.getConsole().getWriter(ERROR_COLOR, FontWeight.NORMAL,
//						FontPosture.REGULAR)) {
//					err.printStackTrace(writer);
//					err.printStackTrace();
//				}
//			if (SHOW_ON_ERROR.get())
//				Application.displayConsole();
//		}
	}

	public static final BooleanProperty debuggingEnabledProperty() {
		return DEBUGGING_ENABLED_PROPERTY;
	}

	public static final boolean isDebuggingEnabled() {
		return debuggingEnabledProperty().get();
	}

	public static final void setDebuggingEnabled(final boolean debuggingEnabled) {
		debuggingEnabledProperty().set(debuggingEnabled);
	}

}
