package pala.apps.arlith.backend.parsers.markup.tokens;

import java.util.HashMap;
import java.util.Map;

import pala.apps.arlith.backend.parsers.markup.MarkupText;
import pala.apps.arlith.backend.parsers.markup.tags.MarkupClosingTag;
import pala.apps.arlith.backend.parsers.markup.tags.MarkupOpeningTag;
import pala.apps.arlith.backend.parsers.markup.tags.MarkupSTElement;
import pala.apps.arlith.backend.parsers.markup.tags.MarkupTag;
import pala.apps.arlith.backend.parsers.markup2.MarkupParserException;
import pala.libs.generic.parsing.BufferedParser;
import pala.libs.generic.streams.CharacterStream;
import pala.libs.generic.streams.PeekableCharacterStream;

public class MarkupTokenizer extends BufferedParser<MarkupToken> {

	private final PeekableCharacterStream in;

	public MarkupTokenizer(CharacterStream str) {
		in = PeekableCharacterStream.from(str);
	}

	@Override
	protected MarkupToken read() {
		switch (in.peek()) {
		case '<':
			return parseTag();
		case -1:
			return null;
		default:
			return parseText();
		}
	}

	private void cw() {
		while (Character.isWhitespace(in.peek()))
			in.next();
	}

	/**
	 * Expects to be called with the next character being the opening '<' of a tag.
	 * 
	 * @return A parsed {@link MarkupTag}.
	 */
	private MarkupTag parseTag() {
		in.next();// Consume opening '<'
		boolean closing = false;
		cw();
		if (closing = (in.peek() == '/')) {
			in.next();
			cw();
		}
		String name = parseName();
		if (name == null && !closing)
			throw new MarkupParserException("Encountered an unnamed opening tag.");
		cw();
		Map<String, String> attribs = parseAttribs();
		cw();
		boolean whole;
		if (whole = in.peek() == '/')
			in.next();
		if (in.peek() != '>')
			throw new MarkupParserException("Expected a closing chevron for the end of a Markup element.");
		in.next();
		return whole ? new MarkupSTElement(name, attribs)
				: closing ? new MarkupClosingTag(name) : new MarkupOpeningTag(name, attribs);
	}

	private MarkupText parseText() {
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		while (true) {
			if (in.peek() == '\\') {
				if (!(escaped ^= true))
					sb.append('\\');
			} else if (in.peek() == '<')
				if (escaped) {
					sb.append('<');
					escaped = false;
				} else
					return new MarkupText(sb.toString());
			else {
				if (escaped) {
					sb.append('\\');
					escaped = false;
				}
				if (in.peek() != -1)
					sb.append(in.peekChar());
				else
					return new MarkupText(sb.toString());
			}
			in.next();
		}
	}

	private String parseName() {
		if (!isNameChar(in.peek()))
			return null;
		StringBuilder sb = new StringBuilder();
		do
			sb.append(in.nextChar());
		while (isNameChar(in.peek()));
		return sb.toString();
	}

	private Map<String, String> parseAttribs() {
		Map<String, String> strings = new HashMap<>();
		while (true) {
			cw();
			if (in.peek() == -1)
				throw new MarkupParserException(
						"Expected either an attribute or the closing '>' of an opening tag name. Instead, the end of the input was reached.");
			else if (isNameChar(in.peek())) {
				String name = parseName();
				if (name == null)
					throw new MarkupParserException("The name for an attribute cannot be empty.");
				cw();
				if (in.peek() == '=') {
					in.next();
					cw();
					if (in.peek() == '"')
						strings.put(name, parseQuote());
					else
						strings.put(name, parseWord());
				} else
					strings.put(name, null);
			} else
				return strings;
		}
	}

	private String parseWord() {
		StringBuilder sb = new StringBuilder();
		while (isNameChar(in.peek()))
			sb.append((char) in.next());
		return sb.toString();
	}

	private String parseQuote() {
		if (in.peek() != '"')
			throw new MarkupParserException(
					"Expected an opening quotation mark for a text value but got: " + in.peekChar());
		in.next();
		StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		while (true) {
			if (in.peek() == '\\') {
				if (!(escaped ^= true))
					sb.append('\\');
			} else if (in.peek() == '"')
				if (escaped) {
					sb.append('"');
					escaped = false;
				} else {
					in.next();// Consume closing quotation char.
					return sb.toString();
				}
			else if (in.peek() == -1)
				throw new MarkupParserException(
						"Expected a quotation mark to close a quoted value, but encountered the end of input.");
			else
				sb.append(in.peekChar());
			in.next();

		}
	}

	private static boolean isNameChar(int chr) {
		return Character.isAlphabetic(chr) || Character.isDigit(chr) || chr == '-' || chr == '$' || chr == '_';
	}

}
