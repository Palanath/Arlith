package pala.apps.arlith.api.parsers.markup.tags;

import java.util.Map;

public class MarkupOpeningTag implements MarkupTag {

	private final String name;
	private final Map<String, String> attributes;

	public MarkupOpeningTag(String name, Map<String, String> attributes) {
		this.name = name;
		this.attributes = attributes;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getName() {
		return name;
	}

}
