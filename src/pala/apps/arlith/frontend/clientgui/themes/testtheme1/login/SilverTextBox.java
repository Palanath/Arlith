package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.javafx.bindings.BindingTools;
import pala.libs.generic.util.Gateway;

public class SilverTextBox extends VBox {

	private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.hsb(0, 0, .7));
	private final DoubleProperty hue = new SimpleDoubleProperty(0);
	{
		BindingTools.bindBidirectional(color, Gateway.from(a -> Color.hsb(a.doubleValue(), .2, .7), a -> a.getHue()),
				hue);
	}

	private Color getFocusedBackgroundColor() {
		Color c = getColor();
		return c.deriveColor(0, .9, 1.1, 1);
	}

	private Color getFocusedLineColor() {
		Color c = getColor();
		return c.deriveColor(0, 3.4, 1.25, 1);
	}

	private final Text prompt = new Text(), asterisk = new Text("*");
	private final HBox promptBox = new HBox(2, prompt);
	private final TextField input;
	private final Line line = new Line();

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
		InvalidationListener il = observable -> input
				.setBackground(input.isFocused() ? FXTools.getBackgroundFromColor(getFocusedBackgroundColor())
						: FXTools.getBackgroundFromColor(getColor()));
		input.focusedProperty().addListener(il);
		color.addListener(il);

		input.setBackground(FXTools.getBackgroundFromColor(getColor()));
		trans.setOnFinished(event -> {
			if (!input.isFocused())
				line.setStroke(Color.TRANSPARENT);
		});
		input.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				line.setStroke(getFocusedLineColor());
				trans.setRate(1);
				trans.play();
			} else {
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

	public final ObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public final Color getColor() {
		return this.colorProperty().get();
	}

	public final void setColor(final Color color) {
		this.colorProperty().set(color);
	}

	public final DoubleProperty hueProperty() {
		return this.hue;
	}

	public final double getHue() {
		return this.hueProperty().get();
	}

	public final void setHue(final double hue) {
		this.hueProperty().set(hue);
	}

}
