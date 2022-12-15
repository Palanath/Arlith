package pala.apps.arlith.api.parsers.markup;

import java.util.ArrayList;
import java.util.List;

import pala.apps.arlith.api.parsers.markup.tags.MarkupClosingTag;
import pala.apps.arlith.api.parsers.markup.tags.MarkupOpeningTag;
import pala.apps.arlith.api.parsers.markup.tags.MarkupSTElement;
import pala.apps.arlith.api.parsers.markup.tokens.MarkupToken;
import pala.apps.arlith.api.parsers.markup.tokens.MarkupTokenizer;
import pala.libs.generic.parsing.BufferedParser;
import pala.libs.generic.streams.CharacterStream;

public class MarkupParser extends BufferedParser<MarkupNode> {

	private final MarkupTokenizer in;

	public MarkupParser(CharacterStream str) {
		in = new MarkupTokenizer(str);
	}

	@Override
	protected MarkupNode read() {
		MarkupToken token = in.peek();
		if (token instanceof MarkupText || token instanceof MarkupSTElement) {
			in.next();
			return (MarkupNode) token;
		} else if (token instanceof MarkupOpeningTag)
			return parseElement();
		else if (token == null)
			return null;
		else
			throw new MarkupParserException("Found an invalid token.");

	}

	private MarkupElement parseElement() {
		MarkupToken t = in.next();
		if (t == null)
			throw new MarkupParserException("End of input reached.");
		else if (!(t instanceof MarkupOpeningTag))
			throw new MarkupParserException("Found an invalid token.");

		List<MarkupNode> children = new ArrayList<>();

		MarkupOpeningTag o = (MarkupOpeningTag) t;
		while (!(in.peek() instanceof MarkupClosingTag)) {
			if (in.peek() == null)
				throw new MarkupParserException("End of input reached before element: " + o.getName() + " was closed.");
			children.add(read());
		}
		MarkupClosingTag c = (MarkupClosingTag) in.next();
		if (!(c.isAnonymous() || o.getName().equals(c.getName())))
			throw new MarkupParserException(
					"Mismatched closing tag for MarkupElement with opening tag: " + o.getName() + '.');
		return new MarkupElement(o.getName(), children, o.getAttributes());
	}

}
