package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import pala.libs.generic.javafx.FXTools;

public class NiceLookingButton extends Button {

	public NiceLookingButton(Color activeColor) {
		setActiveColor(activeColor);
	}

	public NiceLookingButton() {
	}

	private final ObjectProperty<Color> buttonActiveColor = new SimpleObjectProperty<>(Color.hsb(209, .88, 1));

	public Color getActiveColor() {
		return buttonActiveColor.get();
	}

	/**
	 * Sets the active color of the button, which is used as the color of the button
	 * when it is pressed. The non-pressed button color, and the color of the button
	 * when it is hovered over (the highlight color), are derived from this (through
	 * interpolating with the color {@link Color#WHITE}).
	 * 
	 * @param color The active color of the button, used as the button's background
	 *              when it's being pressed and to derive the other background
	 *              colors of this button (that are used in other scenarios).
	 */
	public void setActiveColor(Color color) {
		buttonActiveColor.set(color);
	}

	private Color getButtonBaseColor() {
		return getActiveColor().interpolate(Color.WHITE, .51136363636);
	}

	private Color getHighlightColor() {
		return getActiveColor().interpolate(Color.WHITE, .65909090909);
	}

	{
		setBackground(FXTools.getBackgroundFromColor(getButtonBaseColor()));
		hoverProperty().addListener((observable, oldValue, newValue) -> {
			if (!isPressed())
				if (newValue)
					setBackground(FXTools.getBackgroundFromColor(getHighlightColor()));
				else
					setBackground(FXTools.getBackgroundFromColor(getButtonBaseColor()));
		});
		armedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
				setBackground(FXTools.getBackgroundFromColor(getActiveColor()));
			else if (isHover())
				setBackground(FXTools.getBackgroundFromColor(getHighlightColor()));
			else
				setBackground(FXTools.getBackgroundFromColor(getButtonBaseColor()));
		});
	}
}
