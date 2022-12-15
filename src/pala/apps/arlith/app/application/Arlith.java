package pala.apps.arlith.app.application;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public final class Arlith {

	private static final Image WINDOW_ICON = new Image("pala/apps/arlith/logo.png");
	public static final Color DEFAULT_BASE_COLOR = Color.GOLD, DEFAULT_ACTIVE_COLOR = Color.RED;
	private static Color baseColor = DEFAULT_BASE_COLOR, activeColor = DEFAULT_ACTIVE_COLOR;

	public static Image getWindowIcon() {
		return WINDOW_ICON;
	}

	public static void displayConsole() {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(Arlith::displayConsole);
		else {
			System.out.println("Console should be shown to user at this point.");
		}
	}

	public static Color getBaseColor() {
		return baseColor;
	}

	public static void setBaseColor(Color baseColor) {
		Arlith.baseColor = baseColor;
	}

	public static Color getActiveColor() {
		return activeColor;
	}

	public static void setActiveColor(Color activeColor) {
		Arlith.activeColor = activeColor;
	}

}
