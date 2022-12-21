package pala.apps.arlith.backend.parsers.markup.tags;

public class MarkupClosingTag implements MarkupTag {
	private final String name;

	public MarkupClosingTag(String name) {
		this.name = name;
	}

	public MarkupClosingTag() {
		this(null);
	}

	public boolean isAnonymous() {
		return name == null;
	}

	public String getName() {
		return name;
	}

}
