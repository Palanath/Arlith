package pala.apps.arlith.frontend.clientgui.themes.gray.login;

import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.libs.generic.javafx.FXTools;

public class SilverTextBox extends VBox {

	private static final double DEFAULT_HUE = 0, DEFAULT_SATURATION = 0, DEFAULT_BRIGHTNESS = 1;

	public void resetColor() {
		setHue(DEFAULT_HUE);
		setSaturation(DEFAULT_SATURATION);
		setBrightness(DEFAULT_BRIGHTNESS);
	}

	private final DoubleProperty hue = new SimpleDoubleProperty(DEFAULT_HUE),
			saturation = new SimpleDoubleProperty(DEFAULT_SATURATION),
			brightness = new SimpleDoubleProperty(DEFAULT_BRIGHTNESS);

	private Color getLineColor() {
		return Color.hsb(hue.get(), saturation.get(), brightness.get());
	}

	private Color getActiveBackgroundColor() {
		return Color.hsb(hue.get(), .4 * saturation.get(), .75 * brightness.get());
	}

	private Color getUnfocusedBackgroundColor() {
		return Color.hsb(hue.get(), .3 * saturation.get(), .65 * brightness.get());
	}

	public void showInformation() {
		setShowInformation(true);
	}

	public void hideInformation() {
		setShowInformation(false);
	}

	private final Text prompt = new Text(), asterisk = new Text("*"), information = new Text("");
	private final StackPane informationBox = new StackPane(information);
	private final HBox promptBox = new HBox(2, prompt);
	private final TextField input;
	private final Line line = new Line();

	private final BooleanProperty necessary = new SimpleBooleanProperty(),
			showInformation = new SimpleBooleanProperty();

	public Text getInformationText() {
		return information;
	}

	{
		asterisk.setFill(Color.ORANGERED);
		asterisk.setFont(Font.font(null, FontWeight.BOLD, -1));
		InvalidationListener extraTextsListener = a -> {
			if (necessary.get())
				if (showInformation.get())
					promptBox.getChildren().setAll(prompt, asterisk, informationBox);
				else
					promptBox.getChildren().setAll(prompt, asterisk);
			else if (showInformation.get())
				promptBox.getChildren().setAll(prompt, informationBox);
			else
				promptBox.getChildren().setAll(prompt);
		};
		necessary.addListener(extraTextsListener);
		showInformation.addListener(extraTextsListener);

		HBox.setHgrow(informationBox, Priority.ALWAYS);
		StackPane.setAlignment(information, Pos.CENTER_RIGHT);

		ObjectBinding<Color> colorBinding = Bindings.createObjectBinding(() -> getLineColor(), hue, saturation,
				brightness);
		information.fillProperty().bind(colorBinding);
		line.strokeProperty().bind(colorBinding);
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
		this(null, password, necessary);
	}

	public SilverTextBox(String prompt) {
		this(prompt, false, false);
	}

	public SilverTextBox(String prompt, boolean password, boolean necessary) {
		this.prompt.setText(prompt);
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
		input.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			return FXTools.getBackgroundFromColor(
					input.isFocused() ? getActiveBackgroundColor() : getUnfocusedBackgroundColor());
		}, hue, brightness, saturation, input.focusedProperty()));
		trans.setOnFinished(event -> {
			if (!input.isFocused())
				line.setOpacity(0);
		});
		input.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				line.setOpacity(1);
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
		line.setOpacity(0);
	}

	public final DoubleProperty hueProperty() {
		return this.hue;
	}

	public final double getHue() {
		return this.hueProperty().get();
	}

	/**
	 * <p>
	 * Sets the color of this {@link SilverTextBox} via hue. This method sets the
	 * {@link #hueProperty()} of this {@link SilverTextBox} to the specified value,
	 * unless the specified value and sets the saturation to <code>1</code>.
	 * </p>
	 * 
	 * @param hue The that the text box will be colored as.
	 */
	public final void colorTextBox(double hue) {
		setHue(hue);
		setSaturation(1);
	}

	public void setHue(double hue) {
		hueProperty().set(hue);
	}

	public final BooleanProperty showInformationProperty() {
		return this.showInformation;
	}

	public final boolean isShowInformation() {
		return this.showInformationProperty().get();
	}

	public final void setShowInformation(final boolean showInformation) {
		this.showInformationProperty().set(showInformation);
	}

	public final DoubleProperty brightnessProperty() {
		return this.brightness;
	}

	public final double getBrightness() {
		return this.brightnessProperty().get();
	}

	public final void setBrightness(final double brightness) {
		this.brightnessProperty().set(brightness);
	}

	public final DoubleProperty saturationProperty() {
		return this.saturation;
	}

	public final double getSaturation() {
		return this.saturationProperty().get();
	}

	public final void setSaturation(final double saturation) {
		this.saturationProperty().set(saturation);
	}

}
