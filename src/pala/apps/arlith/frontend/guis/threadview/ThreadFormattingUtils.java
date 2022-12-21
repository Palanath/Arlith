package pala.apps.arlith.frontend.guis.threadview;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pala.apps.arlith.backend.client.api.ClientMessage;
import pala.apps.arlith.backend.parsers.markup2.Markup2Token;
import pala.apps.arlith.backend.parsers.markup2.Markup2Tokenizer;
import pala.apps.arlith.frontend.guis.threadview.ThreadFormattingUtils.TextStyling.Change;
import pala.libs.generic.streams.CharacterStream;

public final class ThreadFormattingUtils {

	private ThreadFormattingUtils() {
	}

	public static Node render(ClientMessage msg) {
		TextFlow tf = new TextFlow();
		tf.getChildren().setAll(renderTexts(msg));
		return tf;
	}

	public static List<? extends Node> renderTexts(ClientMessage msg) {
		try {
			List<Markup2Token> tokens = new Markup2Tokenizer(CharacterStream.from(msg.getText())).parseAll();
			List<Text> nodes = new ArrayList<>();

			class C {
				final Change ch;
				final String type;

				C(Change ch, String type) {
					this.ch = ch;
					this.type = type;
				}

			}

			Stack<C> changes = new Stack<>();
			TextStyling style = new TextStyling();
			for (Markup2Token t : tokens) {
				switch (t.getType()) {
				case TEXT:
					Text item = new Text(t.getText());
					style.styleNode(item);
					nodes.add(item);
					break;
				case ANONYMOUS_CLOSING_TAG:
					if (!changes.isEmpty())// !Malformation.
						changes.pop().ch.perform(false);
					break;
				case CLOSING_TAG:
					CLOSING_TAG: for (ListIterator<C> itr = changes.listIterator(changes.size()); itr.hasPrevious();) {
						C elem = itr.previous();
						if (elem.type.equals(t.getName())) {// This relies on the notion that changes do not "stack"
															// (i.e.,
															// applying the same change twice has is equivalent to
															// applying
															// it once).
							itr.remove();
							elem.ch.perform(false);
							for (; itr.hasNext(); itr.next().ch.perform(true))
								;
							break CLOSING_TAG;
						}
					}
					break;
				default:
					// Opening tag.
					switch (t.getName().toLowerCase()) {
					case "color":
					case "c":
						String v = t.getAttributes().get("v");
						if (v == null)// Malformation.
							break;
						Color c;
						try {
							c = Color.web(v);
						} catch (IllegalArgumentException e) {
							break;
						}
						changes.push(new C(style.setColor(c), t.getName()));
						continue;
					case "size":
					case "s":
						v = t.getAttributes().get("v");
						if (v == null)
							break;
						double size;
						try {
							size = Double.parseDouble(v);
						} catch (NumberFormatException e) {
							break;
						}
						changes.push(new C(style.setFontSize(size), t.getName()));
						continue;
					case "weight":
					case "w":
						v = t.getAttributes().get("v");
						if (v == null)
							break;
						FontWeight fw = FontWeight.findByName(v);
						if (fw == null)
							try {
								fw = FontWeight.findByWeight(Integer.parseInt(v));
							} catch (NumberFormatException e) {
								break;
							}
						changes.push(new C(style.setFontWeight(fw), t.getName()));
						continue;
					case "posture":
					case "p":
						v = t.getAttributes().get("v");
						if (v == null)
							break;
						FontPosture fp = FontPosture.findByName(v);
						if (fp == null)
							break;
						changes.push(new C(style.setPosture(fp), t.getName()));
						continue;
					case "bold":
					case "b":
						changes.push(new C(style.setFontWeight(FontWeight.BOLD), t.getName()));
						continue;
					case "italic":
					case "italicized":
					case "i":
						changes.push(new C(style.setPosture(FontPosture.ITALIC), t.getName()));
						continue;
					case "family":
					case "f":
						v = t.getAttributes().get("v");
						if (v == null)
							break;
						changes.push(new C(style.setFontFamily(v), t.getName()));
						continue;

					case "st":
					case "strikethrough":
					case "strike":
						changes.push(new C(style.setStrikethrough(), t.getName()));
						continue;
					case "u":
					case "underline":
						changes.push(new C(style.setUnderline(), t.getName()));
						continue;
					}
					// Push a change that does nothing to "parse" through this tag, in case the user
					// has entered a tag that is invalid (and then they close it later with an
					// anonymous tag). Anonymous closing tags will close this "do-nothing" tag.
					changes.push(new C(a -> {
					}, t.getName()));
				}
			}
			return nodes;
		} catch (Exception e) {
			List<Node> l = new ArrayList<>();
			Text t = new Text(msg.getText());
			l.add(new TextStyling().styleNode(t));
			return l;
		}
	}

	public static class TextStyling {
		private Color color;
		private String family;
		private FontWeight weight;
		private FontPosture posture;
		private double size = ThreadViewPage.DEFAULT_FONT_SIZE;
		private boolean underline, strikethrough;

		public interface Change {
			void perform(boolean forwards);
		}

		public Text styleNode(Text text) {
			if (color != null) {
				text.setStyle("-fx-fill:reset;");
				text.setFill(color);
			}
			text.setFont(Font.font(family, weight, posture, size));
			text.setUnderline(underline);
			text.setStrikethrough(strikethrough);
			return text;
		}

		public Color getColor() {
			return color;
		}

		public Change setColor(Color color) {
			Color c = this.color;
			this.color = color;
			return f -> this.color = f ? color : c;
		}

		public Change setFontSize(double size) {
			double s = this.size;
			this.size = size;
			return f -> this.size = f ? size : s;
		}

		public Change setFontWeight(FontWeight nw) {
			FontWeight w = weight;
			weight = nw;
			return f -> weight = f ? nw : w;
		}

		public Change setFontFamily(String fam) {
			String f = family;
			family = fam;
			return a -> family = a ? fam : f;
		}

		public Change setPosture(FontPosture pos) {
			FontPosture p = posture;
			posture = pos;
			return f -> posture = f ? pos : p;
		}

		public Change setUnderline() {
			underline = true;
			return f -> underline = f;
		}

		public Change setStrikethrough() {
			strikethrough = true;
			return f -> strikethrough = f;
		}

		@Override
		public TextStyling clone() {
			TextStyling styling = new TextStyling();
			styling.color = color;
			styling.family = family;
			styling.posture = posture;
			styling.size = size;
			styling.weight = weight;
			return styling;
		}
	}

}
