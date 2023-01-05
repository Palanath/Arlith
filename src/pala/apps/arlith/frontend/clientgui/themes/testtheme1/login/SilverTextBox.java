package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.libs.generic.javafx.FXTools;

public class SilverTextBox extends VBox {
	private static final Background FOCUSED_BACKGROUND_COLOR = FXTools.getBackgroundFromColor(Color.gray(.8, .8)),
			UNFOCUSED_BACKGROUND_COLOR = FXTools.getBackgroundFromColor(Color.gray(.7, .7));
	private final Text prompt = new Text(), asterisk = new Text("*");
	private final HBox promptBox = new HBox(2, prompt);
	private final TextField input;
	private final Line line = new Line();
	private static final Color FOCUSED_LINE_COLOR = Color.color(1, 1, 1, .7);

	private final BooleanProperty necessary = new SimpleBooleanProperty();
	{
		asterisk.setFill(Color.ORANGERED);
		asterisk.setFont(Font.font(null, FontWeight.BOLD, -1));
		necessary.addListener((observable, oldValue, newValue) -> {
			if (newValue)
				promptBox.getChildren().setAll(prompt, asterisk);
			else
				promptBox.getChildren().setAll(prompt);
		});
	}

	public boolean isNecessary() {
		return necessary.get();
	}

	public void setNecessary(boolean necessary) {
		this.necessary.set(necessary);
	}

	public BooleanProperty necessaryProperty() {
		return necessary;
	}

	public SilverTextBox(boolean password, boolean necessary) {
		input = password ? new PasswordField() : new TextField();
		getChildren().addAll(promptBox, input, line);
		setNecessary(necessary);

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
		input.setBackground(UNFOCUSED_BACKGROUND_COLOR);
		trans.setOnFinished(event -> {
			if (!input.isFocused())
				line.setStroke(Color.TRANSPARENT);
		});
		input.focusedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
			if (newValue) {
				input.setBackground(FOCUSED_BACKGROUND_COLOR);

				line.setStroke(FOCUSED_LINE_COLOR);
				trans.setRate(1);
				trans.play();
			} else {
				input.setBackground(UNFOCUSED_BACKGROUND_COLOR);

				trans.setRate(-1);
				trans.play();
			}
		});
	}

	public SilverTextBox(boolean password) {
		this(password, false);
	}

	public SilverTextBox() {
		this(false);
	}

	public Text getPrompt() {
		return prompt;
	}

	public TextField getInput() {
		return input;
	}

	public Line getLine() {
		return line;
	}

	{
		prompt.setFill(Color.BLACK);
		line.setStrokeWidth(3);
		line.setEndX(1);// Used to fix a resizing issue (GUI jitters by a few pixels when line has
						// changes from non-zero to zero endX).
		line.setStroke(Color.TRANSPARENT);
	}
}
