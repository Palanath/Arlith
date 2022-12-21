package pala.apps.arlith.backend.parsers.markup;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MarkupElement implements MarkupNode {
	private final String name;
	private final List<MarkupNode> children;
	private final Map<String, String> attributes;

	public MarkupElement(String name, List<MarkupNode> children, Map<String, String> attributes) {
		this.name = name;
		this.children = children;
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public List<MarkupNode> getChildren() {
		return children;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String printAttributes() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> e : attributes.entrySet()) {
			sb.append(' ').append(e.getKey());
			if (e.getValue() != null)
				sb.append("=\"").append(e.getValue().replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
		}

		return sb.toString();

	}

	public String printChildren() {
		StringBuilder sb = new StringBuilder();
		for (MarkupNode n : children)
			sb.append(n.print());
		return sb.toString();
	}

	public String print() {
		return '<' + name + printAttributes() + '>' + printChildren() + "</>";
	}

}
