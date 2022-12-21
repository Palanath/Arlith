package pala.apps.arlith.backend.client.markup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pala.apps.arlith.backend.client.markup.Markup2Token.Type;
import pala.libs.generic.parsing.BufferedParser;
import pala.libs.generic.streams.CharacterStream;
import pala.libs.generic.streams.PeekableCharacterStream;

public class Markup2Tokenizer extends BufferedParser<Markup2Token> {

	private final PeekableCharacterStream in;

	public Markup2Tokenizer(CharacterStream in) {
		this.in = PeekableCharacterStream.from(in);
	}

	@Override
	protected Markup2Token read() {
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
	 * Parses one of {@link Type#OPENING_TAG}, {@link Type#CLOSING_TAG}, and
	 * {@link Type#ANONYMOUS_CLOSING_TAG}.
	 * 
	 * @return The parsed tag.
	 */
	private Markup2Token parseTag() {
		in.next();
		cw();
		if (in.peek() == '/') {
			in.next();// Skip over '/'
			cw();

			if (in.peek() == '>') {
				// Anonymous closing tag.
				in.next();
				return new AnonymousClosingTag();
			} else if (isNameChar(in.peek())) {
				// Closing tag.
				String name = parseName();
				cw();
				if (in.peek() == '>') {
					in.next();
					return new ClosingTag(name);
				} else
					throw new MarkupParserException(in.peek() == -1 ? "End of input reached while parsing a tag."
							: "Unexpected character while parsing a named closing tag: " + (char) in.peek());
			} else
				throw new MarkupParserException(in.peek() == -1 ? "End of input reached while parsing a tag."
						: "Unexpected character when parsing a tag: " + (char) +in.peek());
		} else if (isNameChar(in.peek())) {
			// Opening tag.
			String name = parseName();

			cw();
			if (isNameChar(in.peek())) {
				// Opening tag with attributes.
				Map<String, String> attribs = parseAttribs();
				cw();
				if (in.peek() == '>') {
					in.next();
					return new OpeningTag(name, attribs);
				} else
					throw new MarkupParserException(in.peek() == -1 ? "End of input reached while parsing an opening tag."
							: "Unexpected character reached while parsing an opening tag.");
			} else if (in.peek() == '>') {
				// Opening tag w/o attributes.
				in.next();
				return new OpeningTag(name, Collections.emptyMap());
			} else
				throw new MarkupParserException("Unexpected character when parsing a tag: " + (char) in.peek());
		} else
			throw new MarkupParserException(in.peek() == -1 ? "End of input reached while parsing a tag."
					: "Unexpected character when parsing a tag: " + (char) in.peek());
	}

	private Text parseText() {
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
					return new Text(sb.toString());
			else {
				if (escaped) {
					sb.append('\\');
					escaped = false;
				}
				if (in.peek() != -1)
					sb.append(in.peekChar());
				else
					return new Text(sb.toString());
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
					strings.put(name, in.peek() == '"' ? parseQuote() : parseAttributeValue());
				} else
					strings.put(name, null);
			} else
				return strings;
		}
	}

	private String parseAttributeValue() {
		StringBuilder sb = new StringBuilder();
		while (!Character.isWhitespace(in.peek()) && in.peek() != -1 && in.peek() != '=' && in.peek() != '<'
				&& in.peek() != '>' && in.peek() != '=' && in.peek() != '\'' && in.peek() != '"')
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
