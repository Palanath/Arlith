package pala.apps.arlith.app.guis.login;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import pala.apps.arlith.app.application.ArlithRuntime;
import pala.libs.generic.QuickList;
import pala.libs.generic.backgrounds.Background;
import pala.libs.generic.generators.Generator;

class LogInBackground extends Background {

	private static final double interpolate(double frac) {
		return 1d / (1 + Math.pow(frac / (1 - frac), -2.5));
	}

	private final Generator<Color> CIRCLE_COLOR_GENERATOR = Generator.random(ArlithRuntime.DEFAULT_ACTIVE_COLOR.desaturate(),
			ArlithRuntime.DEFAULT_BASE_COLOR.desaturate());
	private final static double MOVE_RATE = 50;

	private static final double random() {
		return Math.random();
	}

	public LogInBackground() {
	}

	public LogInBackground(Canvas canvas, double targetFramerate) {
		super(canvas, targetFramerate);
	}

	public LogInBackground(Canvas canvas) {
		super(canvas);
	}

	public LogInBackground(double targetFramerate) {
		super(targetFramerate);
	}

	private final QuickList<Circle> circles = new QuickList<>();

	{
		for (int i = 0; i < 12; i++)
			circles.add(new Circle());
	}

	private IntegerProperty steps = new SimpleIntegerProperty();

	{
		steps.bind(targetFramerateProperty().multiply(2.5));
	}

	private class Circle {
		private double x = random() * getWidth(), y = random() * getHeight();
		private double opacity = random() * 2 - 1;
		private final float sizeScale = (float) (2 + random() * 1.5);
		private double vx = (random() * 2 - 1) * MOVE_RATE * 1920 / getWidth(),
				vy = (random() * 2 - 1) * MOVE_RATE * 1080 / getHeight();

		private final Color clr = CIRCLE_COLOR_GENERATOR.next();

		public void draw() {

			if (opacity >= 1) {
				opacity = -1;
				x = random() * getWidth();
				y = random() * getHeight();
				vx = (random() * 2 - 1) * MOVE_RATE * 1920 / getWidth();
				vy = (random() * 2 - 1) * MOVE_RATE * 1080 / getHeight();
			}
			opacity += 1d / steps.get();

			GraphicsContext gc = getContext();
			gc.setFill(new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), Math.abs(Math.abs(opacity) - 1)));
			gc.setLineWidth(1.5);
			gc.setEffect(DEFAULT_CIRCLE_EFFECT);

			double opacity = interpolate(this.opacity / 2 + 0.5);
			double xpos = x + vx * opacity, ypos = y + vy * opacity;

			double diameter = sizeScale * 100 * (getWidth() + getHeight()) / 3000;// == 100 * sizeScale on a 1920x1080
			gc.fillOval(xpos - diameter / 2, ypos - diameter / 2, diameter, diameter);

			gc.setLineWidth(1);
			gc.setEffect(null);
		}
	}

	private final GaussianBlur DEFAULT_CIRCLE_EFFECT = new GaussianBlur(30);
	{
		DEFAULT_CIRCLE_EFFECT.setInput(new Glow(0.5));
	}

	@Override
	protected void draw(GraphicsContext gc) {
		for (Circle c : circles)
			c.draw();
	}

	@Override
	protected void resized(double width, double height, double oldWidth, double oldHeight) {
		for (Circle c : circles) {
			c.x *= width / oldWidth;
			c.y *= height / oldHeight;
		}
	}

	/**
	 * Sets the amount of circles being shown by this background. This method
	 * differs from {@link #setCircles(int)} in that it maintains as many circles
	 * that are currently being used, as it can.
	 *
	 * @param count The new amount of circles that this background will manage.
	 */
	public void setCircleCount(int count) {
		if (count < 0)
			count = 0;
		if (count < circles.size())
			circles.setAll(circles.subList(0, count - 1));
		else
			while (count > circles.size())
				circles.add(new Circle());

	}

	/**
	 * Sets the time that it takes for the circle to fade in (or out, since fading
	 * in and out take the same amount of time). A completely invisible circle will
	 * take <code>seconds * 2</code> seconds to become completely visible (fade in),
	 * then fade back out, becoming completely invisible.
	 *
	 * @param seconds The seconds that the fade animation takes.
	 */
	public void setCircleFadeTime(float seconds) {
		steps.unbind();
		steps.bind(targetFramerateProperty().multiply(seconds));
	}

	/**
	 * Sets the circles being shown by this background.
	 *
	 * @param count The amount of circles.
	 */
	public void setCircles(int count) {
		circles.clear();
		for (int i = 0; i < count; i++)
			circles.add(new Circle());
	}

}
