package pala.apps.arlith.api.parsers.markup2;

import java.util.Map;
import java.util.Map.Entry;

class OpeningTag implements Markup2Token {

	private final String name;
	private final Map<String, String> attributes;

	public OpeningTag(String name, Map<String, String> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	@Override
	public Type getType() {
		return Type.OPENING_TAG;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	private static String quoteEscape(String text) {
		return text.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	@Override
	public String print() {
		StringBuilder sb = new StringBuilder('<').append(name);
		for (Entry<String, String> e : attributes.entrySet()) {
			sb.append(e.getKey());
			if (e.getValue() != null)
				sb.append("=\"").append(quoteEscape(e.getValue())).append('"');
		}
		return sb.append('>').toString();
	}

}
