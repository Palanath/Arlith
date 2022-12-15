package pala.apps.arlith.api.graphics.nodes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import pala.libs.generic.javafx.bindings.BindingTools;

public class SliderBar extends Pane {

	private static final int DEFAULT_BAR_WIDTH = 200;
	private static final double DEFAULT_SELECTOR_WIDTH = 5;

	private final Rectangle rect = new Rectangle(DEFAULT_BAR_WIDTH, 20);
	private final Rectangle selector = new Rectangle(DEFAULT_SELECTOR_WIDTH, 25);

	private final DoubleProperty width = new SimpleDoubleProperty(DEFAULT_BAR_WIDTH);

	public SliderBar(double width) {
		setBarWidth(width);
	}

	public SliderBar() {
	}

	public Rectangle getSelector() {
		return selector;
	}

	public Rectangle getRect() {
		return rect;
	}

	private final DoubleProperty valueProperty = new SimpleDoubleProperty();

	public double getValueProperty() {
		return valueProperty.get();
	}

	public void setValue(double value) {
		if (value < 0)
			value = 0;
		else if (value > 1)
			value = 1;
		valueProperty.set(value);
	}

	public DoubleProperty valueProperty() {
		return valueProperty;
	}

	public double getBarWidth() {
		return width.get();
	}

	public void setBarWidth(double width) {
		this.width.set(width);
	}

	public DoubleProperty barWidthProperty() {
		return width;
	}

	{
		getChildren().setAll(rect, selector);
		setPrefSize(width.get(), 20);
		prefWidthProperty().bind(width);
		rect.widthProperty().bind(prefWidthProperty());
		rect.heightProperty().bind(prefHeightProperty());
		selector.heightProperty().bind(prefHeightProperty().add(10));
		selector.layoutYProperty().bind(prefHeightProperty().subtract(selector.heightProperty()).divide(2));
		selector.layoutXProperty().bind(BindingTools.mask(valueProperty, a -> positionSlider(a.doubleValue())));
		selector.setFill(Color.TRANSPARENT);
		selector.setStroke(Color.BLACK);

		EventHandler<? super MouseEvent> interactionHandler = a -> setValue(a.getX() / getBarWidth());
		rect.setOnMouseClicked(interactionHandler);
		rect.setOnMouseDragged(interactionHandler);
		selector.setMouseTransparent(true);
	}

	/**
	 * Positions the slider so that it is selecting the specified position along the
	 * bar.
	 * 
	 * @param x The position along the bar.
	 */
	private double positionSlider(double x) {
		return x * getBarWidth() - selector.getWidth() / 2;
	}

	public void setFill(Paint paint) {
		rect.setFill(paint);
	}

}
