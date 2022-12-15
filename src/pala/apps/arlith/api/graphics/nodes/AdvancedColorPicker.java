package pala.apps.arlith.api.graphics.nodes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.javafx.bindings.BindingTools;
import pala.libs.generic.util.Gateway;

public class AdvancedColorPicker extends StackPane {

	private static final double DEFAULT_BACKGROUND_OPACITY_THRESHOLD = 0.05;

	private final HBox top = new HBox();
	private final VBox root = new VBox();
	{
		root.setSpacing(20);
		root.getChildren().add(top);
		getChildren().add(root);
	}

	private final DoubleProperty hueValue = new SimpleDoubleProperty(), saturationValue = new SimpleDoubleProperty(1);
	private final ObjectProperty<Color> colorValue = new SimpleObjectProperty<>(Color.RED);
	{
		BindingTools.bindBidirectional(hueValue, new Gateway<Number, Color>() {
			@Override
			public Color to(Number value) {
				return Color.hsb(value.doubleValue(), saturationValue.get(), colorValue.get().getBrightness(),
						colorValue.get().getOpacity());
			}

			@Override
			public Number from(Color value) {
				if (value.getBrightness() == 0 || value.getSaturation() == 0)
					return hueValue.get();
				return value.getHue();
			}
		}, colorValue);
		BindingTools.bindBidirectional(saturationValue, new Gateway<Number, Color>() {
			@Override
			public Color to(Number value) {
				return Color.hsb(hueValue.get(), value.doubleValue(), colorValue.get().getBrightness(),
						colorValue.get().getOpacity());
			}

			@Override
			public Number from(Color value) {
				if (value.getBrightness() == 0 || value.getSaturation() == 0)
					saturationValue.get();
				return value.getSaturation();
			}
		}, colorValue);
		root.backgroundProperty()
				.bind(BindingTools
						.mask(colorValue,
								a -> new Background(new BackgroundFill(
										Color.hsb(getColor().getHue(), getColor().getSaturation(),
												getColor().getBrightness(), DEFAULT_BACKGROUND_OPACITY_THRESHOLD),
										null, null))));
	}

	public Color getColor() {
		return colorValue.get();
	}

	public double getHue() {
		return hueValue.get();
	}

	public void setHue(double value) {
		hueValue.set(value);
	}

	public double getBrightness() {
		return colorValue.get().getBrightness();
	}

	public void setBrightness(double brightness) {
		colorValue.set(Color.hsb(getHue(), getSaturation(), brightness, getOpacityValue()));
	}

	public double getSaturation() {
		return saturationValue.get();
	}

	public void setSaturation(double sat) {
		saturationValue.set(sat);
	}

	public double getOpacityValue() {
		return colorValue.get().getOpacity();
	}

	public void setOpacityValue(double opacity) {
		colorValue.set(Color.hsb(getHue(), getSaturation(), getBrightness(), opacity));
	}

	public DoubleProperty hueProperty() {
		return hueValue;
	}

	public DoubleProperty saturationProperty() {
		return saturationValue;
	}

	public void setColor(Color color) {
		colorValue.set(color);
	}

	public ObjectProperty<Color> colorProperty() {
		return colorValue;
	}

	/**
	 * Returns a color that is fully saturated, fully brightened, and fully opaque,
	 * but has the same hue value as this {@link AdvancedColorPicker} currently
	 * does.
	 * 
	 * @return A color built off of this {@link AdvancedColorPicker}'s hue.
	 */
	public Color getHueColor() {
		return Color.hsb(hueValue.get(), 1, 1);
	}

	private final Circle boxSelector = new Circle(5);

	private void positionSelector(double x, double y) {
		y = 1 - y;
		boxSelector.setTranslateX(x * colboxColor.getWidth() - boxSelector.getRadius());
		boxSelector.setTranslateY(y * colboxColor.getHeight() - boxSelector.getRadius());
	}

	{
		boxSelector.setMouseTransparent(true);
		StackPane.setAlignment(boxSelector, Pos.TOP_LEFT);
		boxSelector.setStroke(Color.WHITE);
		boxSelector.setFill(null);
//		boxSelector.setBlendMode(BlendMode.COLOR_DODGE);
	}

	private final Rectangle colboxColor = new Rectangle(200, 200,
			new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE),
					new Stop(1, getHueColor()))),
			colboxDarkness = new Rectangle(200, 200, new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
					new Stop(0, Color.TRANSPARENT), new Stop(1, Color.BLACK)));
	private final StackPane colbox = new StackPane(colboxColor, colboxDarkness, boxSelector);

	{
		colboxColor.fillProperty().bind(BindingTools.mask(colorValue, a -> new LinearGradient(0, 0, 1, 0, true,
				CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.hsb(getHue(), 1, 1)))));
		top.getChildren().add(colbox);
		colbox.setAlignment(Pos.TOP_LEFT);
		positionSelector(1, 1);
		ChangeListener<Object> boxListener = (observable, oldValue, newValue) -> positionSelector(saturationValue.get(),
				getColor().getBrightness());
		saturationValue.addListener(boxListener);
		colorValue.addListener(boxListener);

		EventHandler<? super MouseEvent> boxMousehandler = event -> {
			double x = event.getX() / 200, y = event.getY() / 200;
			if (x < 0)
				x = 0;
			else if (x > 1)
				x = 1;
			if (y < 0)
				y = 0;
			else if (y > 1)
				y = 1;

			setSaturation(x);
			setBrightness(1 - y);
		};
		colboxDarkness.setOnMouseClicked(boxMousehandler);
		colboxDarkness.setOnMouseDragged(boxMousehandler);
	}

	private final VBox sliderSection = new VBox();
	{
		sliderSection.setSpacing(10);
		top.getChildren().add(sliderSection);
	}

	private static LinearGradient createHueGradient() {
		final int spacing = 256;
		Stop[] stops = new Stop[spacing + 1];
		for (int i = 0; i <= spacing; i++)
			stops[i] = new Stop((double) ((1d / spacing) * i), Color.hsb((int) ((i / (double) spacing) * 360), 1d, 1d));
		return new LinearGradient(0f, 0f, 1f, 0f, true, CycleMethod.NO_CYCLE, stops);
	}

	private final SliderBar huesel = new SliderBar();
	{
		huesel.getSelector().strokeProperty().bind(Bindings.createObjectBinding(this::getHueColor, colorValue));
		BindingTools.bindBidirectional(hueValue, new Gateway<Number, Number>() {
			@Override
			public Number to(Number value) {
				return value.doubleValue() / 360;
			}

			@Override
			public Number from(Number value) {
				return value.doubleValue() * 360;
			}
		}, huesel.valueProperty());
		sliderSection.getChildren().add(huesel);
		huesel.setFill(createHueGradient());
	}

	private final GridPane smallSliders = new GridPane();
	{
		smallSliders.setPrefWidth(200);
		sliderSection.getChildren().add(smallSliders);
		smallSliders.setVgap(5);
		smallSliders.getColumnConstraints().add(new ColumnConstraints(50));
	}

	private final SliderBar redSel = new SliderBar(150), greenSel = new SliderBar(150), blueSel = new SliderBar(150);
	{
		redSel.getSelector().strokeProperty().bind(BindingTools.mask(colorValue, a -> new Color(a.getRed(), 0, 0, 1)));
		redSel.setValue(1);
		greenSel.getSelector().strokeProperty()
				.bind(BindingTools.mask(colorValue, a -> new Color(0, a.getGreen(), 0, 1)));
		blueSel.getSelector().strokeProperty()
				.bind(BindingTools.mask(colorValue, a -> new Color(0, 0, a.getBlue(), 1)));
		redSel.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK),
				new Stop(1, Color.RED)));
		greenSel.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK),
				new Stop(1, Color.GREEN)));
		blueSel.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.BLACK),
				new Stop(1, Color.BLUE)));

		BindingTools.bindBidirectional(redSel.valueProperty(), new Gateway<Number, Color>() {

			@Override
			public Color to(Number value) {
				Color clr = getColor();
				return new Color(value.doubleValue(), clr.getGreen(), clr.getBlue(), clr.getOpacity());
			}

			@Override
			public Number from(Color value) {
				return value.getRed();
			}
		}, colorValue);
		BindingTools.bindBidirectional(greenSel.valueProperty(), new Gateway<Number, Color>() {

			@Override
			public Color to(Number value) {
				Color clr = getColor();
				return new Color(clr.getRed(), value.doubleValue(), clr.getBlue(), clr.getOpacity());
			}

			@Override
			public Number from(Color value) {
				return value.getGreen();
			}
		}, colorValue);
		BindingTools.bindBidirectional(blueSel.valueProperty(), new Gateway<Number, Color>() {

			@Override
			public Color to(Number value) {
				Color clr = getColor();
				return new Color(clr.getRed(), clr.getGreen(), value.doubleValue(), clr.getOpacity());
			}

			@Override
			public Number from(Color value) {
				return value.getBlue();
			}
		}, colorValue);

		smallSliders.add(new Text("Red:"), 0, 0);
		smallSliders.add(new Text("Green:"), 0, 1);
		smallSliders.add(new Text("Blue:"), 0, 2);
		smallSliders.add(redSel, 1, 0);
		smallSliders.add(greenSel, 1, 1);
		smallSliders.add(blueSel, 1, 2);
	}

	private final TextField hexInput = new TextField("#F00");

	{
		BindingTools.bindBidirectional(hexInput.textProperty(), new Gateway<String, Color>() {

			@Override
			public Color to(String value) {
				Color val;
				try {
					val = Color.web(value);
				} catch (IllegalArgumentException e) {
					hexInput.setBorder(FXTools.getBorderFromColor(Color.FIREBRICK));
					return getColor();
				}
				hexInput.setBorder(null);
				return val;
			}

			@Override
			public String from(Color value) {
				return format(value);
			}
		}, colorValue);

		smallSliders.add(new Text("Hex:"), 0, 5);
		smallSliders.add(hexInput, 1, 5);
	}

	{
		top.setSpacing(20);
		top.setPadding(new Insets(20));
	}

	private static String format(double val) {
		String in = Integer.toHexString((int) Math.round(val * 255));
		return in.length() == 1 ? "0" + in : in;
	}

	public static String format(Color value) {
		StringBuilder sb = new StringBuilder();
		sb.append('#').append(format(value.getRed())).append(format(value.getGreen())).append(format(value.getBlue()));
		if (value.getOpacity() != 1)
			sb.append(format(value.getOpacity()).toUpperCase());
		return sb.toString();
	}

	private final Rectangle selectedColor = new Rectangle(50, 20);
	private final Button selectButton = new Button("Select");
	private final StackPane selectButtonContainer = new StackPane(selectButton);
	private final HBox selectedColorContainer = new HBox(new Text("Selected Color: "), selectedColor,
			selectButtonContainer);

	private final BooleanProperty showSelectButton = new SimpleBooleanProperty(true);

	public boolean isShowSelectButton() {
		return showSelectButton.get();
	}

	public void setShowSelectButton(boolean showSelectButton) {
		this.showSelectButton.set(showSelectButton);
	}

	public BooleanProperty showSelectButtonProperty() {
		return showSelectButton;
	}

	{
		HBox.setHgrow(selectButtonContainer, Priority.ALWAYS);
		selectedColorContainer.setPadding(new Insets(0, 20, 20, 20));
		selectedColor.fillProperty().bind(colorValue);
		root.getChildren().add(selectedColorContainer);
		StackPane.setAlignment(selectButton, Pos.CENTER_RIGHT);
		selectButtonContainer.setMinWidth(100);
		selectButton.getStyleClass().add("pop-button");
		root.setAlignment(Pos.TOP_LEFT);
		setAlignment(Pos.TOP_LEFT);

		selectedColorContainer.setMaxWidth(460);
		showSelectButton.addListener((observable, oldValue, newValue) -> {
			if (newValue)
				selectButtonContainer.getChildren().setAll(selectButton);
			else
				selectButtonContainer.getChildren().clear();
		});
	}

	public void setOnAction(EventHandler<ActionEvent> handler) {
		selectButton.setOnAction(handler);
	}

}
