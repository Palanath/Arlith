package pala.apps.arlith.api.parsers.markup2;

import java.util.Map;

class Text implements Markup2Token {

	private final String text;

	public Text(String text) {
		this.text = text;
	}

	@Override
	public Type getType() {
		return Type.TEXT;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public Map<String, String> getAttributes() {
		return null;
	}

	private static String textEscape(String text) {
		return text.replace("\\", "\\\\").replace("<", "\\<");
	}

	@Override
	public String print() {
		return textEscape(text);
	}

}
