package pala.apps.arlith.backend.client.markup;

import java.util.Map;

class ClosingTag implements Markup2Token {

	private final String name;

	public ClosingTag(String name) {
		this.name = name;
	}

	@Override
	public Type getType() {
		return Type.CLOSING_TAG;
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
		return null;
	}

	@Override
	public String print() {
		return "</" + name + '>';
	}

}
