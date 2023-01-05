package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.libs.generic.javafx.FXTools;

public class SilverTextBox extends VBox {
	private final Text prompt = new Text();
	private final TextField input = new TextField();
	private final Line line = new Line();
	private static final Color FOCUSED_LINE_COLOR = Color.color(1, 1, 1, .7);
	{
		prompt.setFill(Color.BLACK);
		getChildren().addAll(prompt, input, line);
		input.setBackground(FXTools.getBackgroundFromColor(Color.gray(.6, .5)));
		line.setStrokeWidth(3);
		line.setEndX(1);// Used to fix a resizing issue (GUI jitters by a few pixels when line has
						// changes from non-zero to zero endX).
		line.setStroke(Color.TRANSPARENT);
		Transition trans = new Transition() {
			{
				setCycleDuration(Duration.seconds(.2));
				setCycleCount(1);
			}

			@Override
			protected void interpolate(double frac) {
				// +1 -1 are used to fix the jittering issue described above.
				line.setEndX(frac * (input.getWidth() - line.getStrokeWidth() - 1) + 1);
			}
		};
		trans.setOnFinished(event -> {
			if (!input.isFocused())
				line.setStroke(Color.TRANSPARENT);
		});
		input.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if (newValue) {
				input.setBackground(FXTools.getBackgroundFromColor(Color.gray(.8, .3)));

				line.setStroke(FOCUSED_LINE_COLOR);
				trans.setRate(1);
				trans.play();
			} else {
				input.setBackground(FXTools.getBackgroundFromColor(Color.gray(.6, .5)));

				trans.setRate(-1);
				trans.play();
			}
		});
	}
}
