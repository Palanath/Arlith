package pala.apps.arlith.backend.parsers.markup.tags;

import java.util.Map;
import java.util.Map.Entry;

import pala.apps.arlith.backend.parsers.markup.MarkupNode;

public class MarkupSTElement extends MarkupOpeningTag implements MarkupNode {

	public MarkupSTElement(String name, Map<String, String> attributes) {
		super(name, attributes);
	}

	@Override
	public String print() {
		return '<' + getName() + ' ' + printAttribs() + "/>";
	}

	private String printAttribs() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> e : getAttributes().entrySet()) {
			sb.append(' ').append(e.getKey());
			if (e.getValue() != null)
				sb.append("=\"").append(e.getValue().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
		}
		return sb.toString();
	}

}
