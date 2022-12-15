package pala.apps.arlith.api.parsers.markup;

import pala.apps.arlith.api.parsers.markup.tokens.MarkupToken;

public class MarkupText implements MarkupNode, MarkupToken {
	private final String value;

	public String getValue() {
		return value;
	}

	public MarkupText(String value) {
		this.value = value;
	}

	@Override
	public String print() {
		return value.replace("\\", "\\\\").replace("<", "\\<").replace(">", "\\>");
	}

}
