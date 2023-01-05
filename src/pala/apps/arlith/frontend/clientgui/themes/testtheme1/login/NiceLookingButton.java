package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import pala.libs.generic.javafx.FXTools;

public class NiceLookingButton extends Button {
	{
		setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		hoverProperty().addListener((observable, oldValue, newValue) -> {
			if (!isPressed())
				if (newValue)
					setBackground(
							FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate().desaturate()));
				else
					setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		});
		armedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue)
				setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE));
			else if (isHover())
				setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate().desaturate()));
			else
				setBackground(FXTools.getBackgroundFromColor(Color.DODGERBLUE.desaturate().desaturate()));
		});
	}
}
