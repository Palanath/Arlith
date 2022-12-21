package pala.apps.arlith.application;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import pala.libs.generic.items.LateLoadItem;

public class JFXArlithRuntime {

	public static final LateLoadItem<Image> MISSING_TEXTURE_IMAGE = new LateLoadItem<>(
			() -> new Image("/pala/apps/arlith/missing-texture.png", false));
	private static final Image WINDOW_ICON = new Image("pala/apps/arlith/logo.png");
	public static final Color DEFAULT_BASE_COLOR = Color.GOLD, DEFAULT_ACTIVE_COLOR = Color.RED;
	private static Color baseColor = DEFAULT_BASE_COLOR, activeColor = DEFAULT_ACTIVE_COLOR;

	public static Color getActiveColor() {
		return activeColor;
	}

	public static Color getBaseColor() {
		return baseColor;
	}

	public static Image getWindowIcon() {
		return WINDOW_ICON;
	}

	public static void setActiveColor(Color activeColor) {
		JFXArlithRuntime.activeColor = activeColor;
	}

	public static void setBaseColor(Color baseColor) {
		JFXArlithRuntime.baseColor = baseColor;
	}

}
