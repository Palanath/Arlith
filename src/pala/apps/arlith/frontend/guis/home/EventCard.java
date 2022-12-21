package pala.apps.arlith.frontend.guis.home;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.FIREBRICK;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import pala.libs.generic.javafx.FXTools;

public class EventCard extends VBox {
	private static final DropShadow COMMON_DEFAULT_DROP_SHADOW_EFFECT = new DropShadow();

	{
		setMaxSize(300, 200);
		setMinSize(300, 200);
		setGlow(null, false);
		setPadding(new Insets(10));
		setAlignment(Pos.CENTER);
		setEffect(COMMON_DEFAULT_DROP_SHADOW_EFFECT);
	}

//	public static DropShadow getGlow(Color color) {
//		DropShadow ds = new DropShadow();
//		ds.setColor(color);
//		return ds;
//	}

	public static EventCard basic(String title, String content) {
		EventCard ec = new EventCard();
		ec.setSpacing(15);

		Text tit = new Text(title);
		tit.setStyle("-fx-font-size:22;");
		tit.setWrappingWidth(260);
		tit.setTextAlignment(TextAlignment.CENTER);

		Text cont = new Text(content);
		cont.setStyle("-fx-font-size:14;");
		cont.setTextAlignment(TextAlignment.CENTER);

		ScrollPane sp = new ScrollPane(cont);
		sp.setPadding(new Insets(0, 5, 0, 5));
		cont.wrappingWidthProperty().bind(sp.widthProperty().subtract(30));
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		VBox.setVgrow(sp, Priority.ALWAYS);

		ec.getChildren().setAll(tit, sp);

		return ec;
	}

	public static EventCard basic(String title, String content, Color color) {
		EventCard ec = basic(title, content);
		ec.setGlow(color);
		return ec;
	}

	public static EventCard basic(String title, String content, Type type) {
		EventCard ec = basic(title, content);
		ec.setGlow(type == null ? BLACK : type.color);
		return ec;
	}

	public void setGlow(Color color) {
		setGlow(color, false);
	}

	public void setGlow(Color color, boolean includeBg) {
//		setEffect(getGlow(color));
		if (color == null) {
			setBorder(null);
			setStyle("-fx-background-color:derive(-stuff-light,15%);-fx-background-radius:20px;-fx-font-weight:bold;");
		} else {
			setBorder(FXTools.getBorderFromColor(color, 2, 20));
			setStyle(
					"-fx-background-color:"
							+ (includeBg ? "ladder(#070707,derive(-stuff-light,15%)," + toHexString(color) + ")"
									: "derive(-stuff-light,15%)")
							+ ";-fx-background-radius:20px;-fx-font-weight:bold;");
		}
	}

	private static String format(double val) {
		String in = Integer.toHexString((int) Math.round(val * 255));
		return in.length() == 1 ? "0" + in : in;
	}

	public static String toHexString(Color value) {
		return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue())
				+ format(value.getOpacity())).toUpperCase();
	}

	public enum Type {
		DIRECT_MESSAGE(FIREBRICK), FRIEND_REQUEST(new Color(152d / 255, 35d / 255, 191d / 255, 1)),
		FEED_POST(new Color(44d / 255, 109d / 255, 215d / 255, 1)), BROADCAST(new Color(219d / 255, 126d / 255, 0, 1)),
		APPLICATION_UPDATES(new Color(217d / 255, 182d / 255, 13d / 255, 1)),
		COMMUNITY_MANAGER(new Color(35d / 255, 191d / 255, 29d / 255, 1));

		private final Color color;

		private Type(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}

	}
}
