package pala.apps.arlith.app.guis;

import java.util.Set;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;
import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.application.ArlithRuntime;
import pala.libs.generic.JavaTools;
import pala.libs.generic.javafx.FXTools;

public final class GUIUtils {

	public static final Duration CLICK_AND_RELEASE_ANIMATION_TIME = Duration.seconds(0.2);

	/**
	 * Creates a new {@link Stage} and styles it to fit with the application. This
	 * function must be called on the application thread.
	 *
	 * @return The newly created {@link Stage}.
	 * @throws InterruptedException In case the thread is interrupted while the FX
	 *                              Application thread makes the stage.
	 */
	public static Stage makeStage() {
		Stage stage = new Stage();
		prepareStage(stage);
		return stage;
	}

	private static final EventHandler<MouseEvent> focusHandler = event -> event.getPickResult().getIntersectedNode()
			.requestFocus();

	public static void prepareStage(Stage stage) {
		stage.getIcons().add(ArlithRuntime.getWindowIcon());
		stage.setFullScreenExitHint("Press F11 to exit fullscreen mode.");
		stage.setTitle("Arlith");
		stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if (event.getCode() == KeyCode.F11) {
				stage.setFullScreen(!stage.isFullScreen());
				event.consume();
			} else if (event.getCode() == KeyCode.PRINTSCREEN) {
				try {
					WritableImage img = stage.getScene().snapshot(
							new WritableImage((int) stage.getScene().getWidth(), (int) stage.getScene().getHeight()));
					ClipboardContent content = new ClipboardContent();
					content.putImage(img);
					Clipboard.getSystemClipboard().setContent(content);
					FXTools.spawnLabelAtMousePos("Screenshot copied to Clipboard!", Color.GREEN, stage);
				} catch (Exception e) {
					Logging.err(
							"Failed to take a screenshot and copy it to the clipboard. The error's stacktrace has been printed.");
					Logging.err(e);
					FXTools.spawnLabelAtMousePos("Screenshot failed...", Color.RED, stage);
				}
			} else if (event.getCode() == KeyCode.F3 && event.isShortcutDown())
				ArlithRuntime.displayConsole();
		});
		stage.sceneProperty().addListener((ChangeListener<Scene>) (observable, oldValue, newValue) -> {
			newValue.removeEventFilter(MouseEvent.MOUSE_PRESSED, focusHandler);
			newValue.addEventFilter(MouseEvent.MOUSE_PRESSED, focusHandler);
		});
	}

	public static ClickAnimation applyClickAnimation(Node clicker, Color from, Color to, Shape... fillables) {
		return applyClickAnimation(clicker, from, to, CLICK_AND_RELEASE_ANIMATION_TIME, fillables);
	}

	public static ClickAnimation applyClickAnimation(Set<? extends Node> clickers, Color from, Color to,
			Shape... fillables) {
		return new ClickAnimation() {
			Color f = from, t = to;
			Transition anim = new Transition() {
				{
					setCycleDuration(CLICK_AND_RELEASE_ANIMATION_TIME);
					setInterpolator(Interpolator.EASE_OUT);
				}

				@Override
				protected void interpolate(double frac) {
					for (Shape s : fillables)
						s.setFill(f.interpolate(t, frac));
				}
			};

			EventHandler<? super MouseEvent> pressHandler = event -> {
				anim.pause();
				for (Shape s : fillables)
					s.setFill(t);
			};
			EventHandler<? super MouseEvent> releaseHandler = event -> {
				anim.pause();
				anim.playFrom(CLICK_AND_RELEASE_ANIMATION_TIME);
			};

			{
				anim.setRate(-1);
				for (Node n : clickers) {
					n.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
					n.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
				}
			}

			@Override
			public void unbind() {
				for (Node n : clickers) {
					n.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
					n.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
				}
			}

			@Override
			public void setFrom(Color color) {
				f = color;
			}

			@Override
			public void setTo(Color color) {
				t = color;
			}
		};
	}

	public static ClickAnimation applyClickAnimation(Node clicker, Color from, Color to, Duration duration,
			Shape... fillables) {
		return applyClickAnimation(clicker, from, to, duration,
				JavaTools.convert(t -> (Fillable) t::setFill, fillables));
	}

	public static ClickAnimation applyClickAnimation(Node clicker, Color from, Color to, Duration duration,
			Fillable... fillables) {

		return new ClickAnimation() {
			Color f = from, t = to;
			Transition anim = new Transition() {
				{
					setCycleDuration(CLICK_AND_RELEASE_ANIMATION_TIME);
					setInterpolator(Interpolator.EASE_OUT);
				}

				@Override
				protected void interpolate(double frac) {
					for (Fillable s : fillables)
						s.fill(f.interpolate(t, frac));
				}
			};

			EventHandler<? super MouseEvent> pressHandler = event -> {
				anim.pause();
				for (Fillable s : fillables)
					s.fill(t);
			};
			EventHandler<? super MouseEvent> releaseHandler = event -> {
				anim.pause();
				anim.playFrom(CLICK_AND_RELEASE_ANIMATION_TIME);
			};

			{
				anim.setRate(-1);
				clicker.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				clicker.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}

			@Override
			public void unbind() {
				clicker.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				clicker.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}

			@Override
			public void setFrom(Color color) {
				f = color;
			}

			@Override
			public void setTo(Color color) {
				t = color;
			}
		};
	}

	public static ClickAnimation applyClickAnimation(Node clicker, Color from, Color to, Fillable... fillables) {
		return applyClickAnimation(clicker, from, to, CLICK_AND_RELEASE_ANIMATION_TIME, fillables);
	}

	// TODO Return an object that can have the handlers be unbound.
	public static ClickAnimation applyClickAnimation(Node clicker, Duration duration, FillItem... fillables) {
		return new ClickAnimation() {

			Transition anim = new Transition() {
				{
					setCycleDuration(CLICK_AND_RELEASE_ANIMATION_TIME);
					setInterpolator(Interpolator.EASE_OUT);
				}

				@Override
				protected void interpolate(double frac) {
					for (FillItem s : fillables)
						s.fill(s.from.interpolate(s.to, frac));
				}
			};

			EventHandler<? super MouseEvent> pressHandler = event -> {
				anim.pause();
				for (FillItem s : fillables)
					s.fill(s.to);
			};
			EventHandler<? super MouseEvent> releaseHandler = event -> {
				anim.pause();
				anim.playFrom(CLICK_AND_RELEASE_ANIMATION_TIME);
			};

			{
				anim.setRate(-1);
				clicker.addEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				clicker.addEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}

			@Override
			public void unbind() {
				clicker.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressHandler);
				clicker.removeEventHandler(MouseEvent.MOUSE_RELEASED, releaseHandler);
			}

			@Override
			public void setFrom(Color color) {

			}

			@Override
			public void setTo(Color color) {

			}
		};
	}

	public interface ClickAnimation {
		void unbind();

		void setFrom(Color color);

		void setTo(Color color);
	}

	public interface Fillable {
		void fill(Color color);
	}

	public static abstract class FillItem implements Fillable {
		private final Color from, to;

		public FillItem(Color from, Color to) {
			this.from = from;
			this.to = to;
		}

		public static FillItem fromShape(Color from, Color to, Shape shape) {
			return new FillItem(from, to) {

				@Override
				public void fill(Color color) {
					shape.setFill(color);
				}
			};
		}
	}

}
