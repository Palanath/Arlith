package pala.apps.arlith.backend.client.markup;

import java.util.Map;

public interface Markup2Token {
	public enum Type {
		OPENING_TAG, TEXT, CLOSING_TAG, ANONYMOUS_CLOSING_TAG;
	}

	/**
	 * <p>
	 * The {@link Type} of this token. This can be used to determine what properties
	 * this token posseses.
	 * </p>
	 * 
	 * @return The {@link Type} of this token.
	 */
	Type getType();

	/**
	 * <p>
	 * The name of this {@link Type#OPENING_TAG opening} or {@link Type#CLOSING_TAG
	 * named closing} tag. ( {@link Type#ANONYMOUS_CLOSING_TAG anonymous closing
	 * tag}s do not have a name associated with them.
	 * </p>
	 * <p>
	 * The name of the following {@link Type#OPENING_TAG opening tag} is portion in
	 * red:
	 * </p>
	 * 
	 * <pre>
	 * <code>&lt;<span style=
	 * "color:red;">name</span> attrib1=true attrib2="some text" attrib3&gt;</code>
	 * </pre>
	 * 
	 * <p>
	 * The name of the following {@link Type#CLOSING_TAG closing tag} is the portion
	 * in red:
	 * </p>
	 * 
	 * <pre>
	 * <code>&lt;<span style="color:red;">name</span>/&gt;</code>
	 * </pre>
	 * 
	 * @return The name of the opening or named closing tag. For
	 */
	String getName();

	/**
	 * <p>
	 * The content of this {@link Type#TEXT} token. This is only valid for
	 * {@link Type#TEXT} tokens.
	 * </p>
	 * 
	 * @return The text contained in this text token.
	 */
	String getText();

	/**
	 * <p>
	 * The attributes contained in this {@link Type#OPENING_TAG}. This is only valid
	 * for {@link Type#OPENING_TAG} tokens, and is <code>null</code> for all other
	 * tokens. For {@link Type#OPENING_TAG} tokens that do not have any attributes,
	 * an empty map is returned. Attributes that do not possess a value will have a
	 * <code>null</code> value in the returned {@link Map}.
	 * </p>
	 * <p>
	 * This method may or may not return an immutable map.
	 * </p>
	 * <p>
	 * The attributes in the following {@link Type#OPENING_TAG opening tag} are the
	 * portions highlighted in red of the following opening tag:
	 * </p>
	 * 
	 * <pre>
	 * <code>&lt;name <span style=
	 * "color: red;">attrib1=true attrib2="some text" attrib3</span>&gt;</code>
	 * </pre>
	 * 
	 * @return All of the attributes contained within this {@link Type#OPENING_TAG}
	 *         token.
	 */
	Map<String, String> getAttributes();

	String print();

}
