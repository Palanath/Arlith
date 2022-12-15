package pala.apps.arlith.api.parsers.markup2;

import java.util.Map;

class AnonymousClosingTag implements Markup2Token {

	@Override
	public Type getType() {
		return Type.ANONYMOUS_CLOSING_TAG;
	}

	@Override
	public String getName() {
		return null;
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
		return "</>";
	}

}
