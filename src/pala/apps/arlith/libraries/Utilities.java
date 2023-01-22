package pala.apps.arlith.libraries;

import static pala.apps.arlith.libraries.Utilities.EmailIssue.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pala.libs.generic.JavaTools;
import pala.libs.generic.strings.StringTools;

/**
 * General {@link Utilities}. This class shouldn't depend on any libraries
 * besides the standard library. See {@link JavaFXUtilities} for JFX utilities.
 * 
 * @author Palanath
 *
 */
public class Utilities {
	public static final String DEFAULT_DESTINATION_ADDRESS = "app.arlith.net";
	public static final int DEFAULT_PORT = 42069;
	public static final LocalDateTime PROGRAM_LAUNCH_TIME = LocalDateTime.now();

	private static int preferredPort = DEFAULT_PORT;
	private static String preferredDestinationAddress = DEFAULT_DESTINATION_ADDRESS;

	private static String[] controlCharacters = { "#", "@", "<", ">" };

	public static String[] getControlCharacters() {
		return Arrays.copyOf(controlCharacters, controlCharacters.length);
	}

	/**
	 * <h1>Overview</h1>
	 * <p>
	 * Determines if a provided username is valid in accordance with the username
	 * specification at <a href=
	 * "https://arlith.net/user-accounts/">https://arlith.net/user-accounts/</a>.
	 * </p>
	 * <p>
	 * This method returns <code>null</code> if the provided username is valid.
	 * Otherwise it returns a {@link UsernameIssue}, describing why the username is
	 * not valid.
	 * </p>
	 * <h2>Implementation Notes</h2>
	 * <p>
	 * The returned {@link UsernameIssue} returns the value <code>-1</code> when
	 * {@link UsernameIssue#getCharpos()} is invoked, <i>unless</i> the value
	 * returned by {@link UsernameIssue#getIssue()} is
	 * {@link Issue#CONTAINED_ILLEGAL_CHARACTER}, in which case the
	 * {@link UsernameIssue#getCharpos()} is the index of the <i>first</i> erroneous
	 * character found from the beginning of the string. When an invalid username is
	 * provided:
	 * </p>
	 * <ol>
	 * <li>If the username is fewer than <code>3</code> characters in length, this
	 * method returns a {@link UsernameIssue} indicative of
	 * {@link Issue#USERNAME_TOO_SHORT};</li>
	 * <li>Otherwise, if the username is more than <code>20</code> characters in
	 * length, this method returns a {@link UsernameIssue} indicative of
	 * {@link Issue#USERNAME_TOO_LONG}.</li>
	 * <li>Otherwise, this method iterates over every character in the provided
	 * {@link String}, from the beginning, and in doing so, upon encountering an
	 * illegal character (one of {@link #getControlCharacters()}), returns a new
	 * {@link UsernameIssue} whose {@link UsernameIssue#getCharpos()} method
	 * reflects the index of that character in the {@link String}. If no illegal
	 * character is found, the method returns <code>null</code>.</li>
	 * </ol>
	 * 
	 * @param username The username to check the validity of.
	 * @return <code>null</code> if the username is valid or, if not valid, a
	 *         {@link UsernameIssue} describing why.
	 */
	public static UsernameIssue checkUsernameValidity(String username) {
		if (username.length() < 3)
			return new UsernameIssue(-1, UsernameIssue.Issue.USERNAME_TOO_SHORT);
		else if (username.length() > 20)
			return new UsernameIssue(-1, UsernameIssue.Issue.USERNAME_TOO_LONG);
		else
			for (int i = 0, j; i < username.length(); i++)
				if ((j = JavaTools.indexOf(String.valueOf(username.charAt(i)), getControlCharacters())) != -1)
					return new UsernameIssue(j, UsernameIssue.Issue.CONTAINED_ILLEGAL_CHARACTER);
		return null;
	}

	public static EmailIssue checkEmailValidity(String email) {
		return checkEmailValidity(email, false);// For leniency, we don't check arbitrary escapes.
	}

	public static EmailIssue checkEmailValidity(String email, boolean checkArbitraryEscapes) {
		if (email.isEmpty())
			return new EmailIssue(EmailIssue.Issue.EMPTY_EMAIL, "", "");
		else {
			int nextcharpos;
			StringBuilder lp = new StringBuilder();
			if (email.startsWith("\"")) {
				boolean escaped = false;
				for (int i = 1,
						ch = i < email.length() ? email.charAt(i) : -1;; ch = ++i < email.length() ? email.charAt(i)
								: -1)
					if (ch == -1)
						return new EmailIssue(EmailIssue.Issue.NONTERMINATED_QUOTED_LOCALPART, email, "");
					else if (ch == '\\') {
						if (!(escaped ^= true))
							lp.append('\\');
					} else if (ch == '"')
						if (escaped) {
							escaped = false;
							lp.append('"');
						} else // End of local part(?)
						if (i + 1 == email.length())
							return new EmailIssue(EmailIssue.Issue.END_OF_STRING_FOUND_BEFORE_AT, email, "");
						else {
							nextcharpos = i + 1;
							break;
						}
					else {
						if (escaped)
							if (checkArbitraryEscapes)
								return new EmailIssue(i, EmailIssue.Issue.ARBITRARY_ESCAPE, null, null);
							else
								lp.append('\\');
						escaped = false;
						lp.append((char) ch);
					}
			} else {
				boolean prevCharWasDot = false;
				LOOP: for (int i = 0, ch = email.charAt(i);; ch = ++i < email.length() ? email.charAt(i) : -1)
					if (ch <= 'Z' && ch >= 'A' || ch <= 'z' && ch >= 'a' || ch <= '9' && ch >= '0') {
						lp.append((char) ch);
						prevCharWasDot = false;
					} else
						switch (ch) {
						case '!':
						case '#':
						case '$':
						case '%':
						case '&':
						case '\'':
						case '*':
						case '+':
						case '-':
						case '/':
						case '=':
						case '?':
						case '^':
						case '_':
						case '`':
						case '{':
						case '|':
						case '~':
							prevCharWasDot = false;
							lp.append((char) ch);
							break;
						case -1:
							return new EmailIssue(EmailIssue.Issue.END_OF_STRING_FOUND_BEFORE_AT, email, "");
						case '.':
							if (i == 0)
								return new EmailIssue(EmailIssue.Issue.LOCAL_PART_BEGINS_WITH_PERIOD, ".", null);
							else if (prevCharWasDot)
								return new EmailIssue(i, EmailIssue.Issue.TOUCHING_DOTS_IN_LOCAL_PART, null, null);
							lp.append('.');
							prevCharWasDot = true;
							break;
						case '@':
							if (prevCharWasDot)
								return new EmailIssue(i - 1, EmailIssue.Issue.LOCAL_PART_ENDS_WITH_PERIOD,
										email.substring(0, i), null);
							nextcharpos = i;
							break LOOP;
						default:
							return new EmailIssue(i, EmailIssue.Issue.ILLEGAL_CHARACTER, null, null);
						}
			}
			if (email.charAt(nextcharpos) != '@')
				return new EmailIssue(nextcharpos, EmailIssue.Issue.LOCAL_PART_NOT_FOLLOWED_BY_AT,
						email.substring(0, nextcharpos), null);
			String localpart = email.substring(0, nextcharpos), domain = email.substring(nextcharpos + 1);
			// Parse domain name.

			if (domain.startsWith("["))
				if (!domain.endsWith("]"))
					return new EmailIssue(EmailIssue.Issue.DOMAIN_HAS_NO_CLOSING_BRACKET, localpart, domain);
				else {
					String ipaddrStr = domain.substring(1, domain.length() - 1);// Str between brackets.
					if (ipaddrStr.isEmpty())
						// The position is the opening bracket.
						return new EmailIssue(localpart.length() + 1, EmailIssue.Issue.DOMAIN_USED_BRACKETS_BUT_NO_IP,
								localpart, domain);
					else if (ipaddrStr.toLowerCase().startsWith("ipv6::"))
						return new EmailIssue(EmailIssue.Issue.IPV6_USED, localpart, domain);
					else {
						// IPv4
						String[] pieces = StringTools.split(ipaddrStr, ".");
						if (pieces.length == 1)
							// The character at position localpart.length() is the @ symbol (because it
							// immediately follows the local-part).
							// The character after that is the opening bracket we already checked for. And
							// the character after that is the start of what should be the IP.
							return new EmailIssue(localpart.length() + 2,
									EmailIssue.Issue.DOMAIN_USED_BRACKETS_BUT_INVALID_IP, localpart, domain);
						else if (pieces.length != 4)
							return new EmailIssue(EmailIssue.Issue.DOMAIN_IPV4_HAS_INVALID_NUMBER_OF_OCTETS, localpart,
									domain);
						else
							// runningPos stores exactly how many characters in the parts we've parsed past
							// already. When the body of the loop runs for the third octet of the email:
							// pala@[192.168.1.1], runningPos will equal "192.".length() + "168.".length().
							for (int i = 0, runningPos = 0; i < pieces.length; runningPos += pieces[i++].length() + 1) {
								// Parse decimal IPv4 Addresses.
								// Make sure it's the right number of characters. (We don't allow prefixing with
								// 0 for octal or with 0x for hex).
								if (pieces[i].length() > 3 || pieces[i].isEmpty())
									return new EmailIssue(localpart.length() + 2 + runningPos,
											EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
								else // Validate that piece is a number from 0 to 255.
								if (pieces[i].length() == 1) {// If octet is 1 char long.
									// Can be any number from 0 to 9.
									if (pieces[i].charAt(0) < '0' || pieces[i].charAt(0) > '9')
										return new EmailIssue(localpart.length() + 2 + runningPos,
												EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
								} else if (pieces[i].length() == 2) {
									// First char can be any number from 1-9. Seconds can be any from 0-9
									if (pieces[i].charAt(0) < '1' || pieces[i].charAt(0) > '9')
										return new EmailIssue(localpart.length() + 2 + runningPos,
												EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
									else if (pieces[i].charAt(1) < '0' || pieces[i].charAt(0) > '9')
										return new EmailIssue(localpart.length() + 2 + runningPos,
												EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
								} else if (pieces[i].length() == 3)
									if (pieces[i].charAt(0) == '1') {
										// Second and third chars can go from 0-9
										if (pieces[i].charAt(1) < '0' || pieces[i].charAt(1) > '9'
												|| pieces[i].charAt(2) < '0' || pieces[i].charAt(2) > '9')
											return new EmailIssue(localpart.length() + 2 + runningPos,
													EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
									} else if (pieces[i].charAt(0) == '2') {
										if (pieces[i].charAt(1) == '5') {
											// Third char can be from 0-5
											if (pieces[i].charAt(2) < '0' || pieces[i].charAt(2) > '5')
												return new EmailIssue(localpart.length() + 2 + runningPos,
														EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
										} else // Second char can be from 0-4 and third can be from 0-9
										if (pieces[i].charAt(1) < '0' || pieces[i].charAt(1) > '4'
												|| pieces[i].charAt(2) < '0' || pieces[i].charAt(2) > '9')
											return new EmailIssue(localpart.length() + 2 + runningPos,
													EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
									} else
										return new EmailIssue(localpart.length() + 2 + runningPos,
												EmailIssue.Issue.DOMAIN_IPV4_INVALID_OCTET, localpart, domain);
							}
						return null;
					}
				}
			else if (domain.isEmpty())
				return new EmailIssue(EmailIssue.Issue.DOMAIN_EMPTY, localpart, "");
			else {
				List<String> parts = new ArrayList<>();
				int previousDot = -1;// Used to determine where each part lies. Conceptually, the "first dot" is
										// "before" the start of the domain name (this is for the sake of parsing and
										// checking hyphens).
				for (int nextchar = domain.charAt(nextcharpos = 0);; nextchar = ++nextcharpos >= domain.length() ? -1
						: domain.charAt(nextcharpos)) {
					if (nextchar >= 'A' && nextchar <= 'Z' || nextchar >= 'a' && nextchar <= 'z'
							|| nextchar >= '0' && nextchar <= '9') {
						if (nextcharpos - previousDot > 63)
							// Too many octets.
							return new EmailIssue(nextcharpos + localpart.length() + 1,
									EmailIssue.Issue.TOO_MANY_OCTETS_IN_DOMAIN_NAME_LABEL, localpart, domain);
					} else if (nextchar == '-') {
						// Make sure that we're not at the beginning of the part. This is the case if
						// nextcharpos is immediately after the "previous dot." For example, if
						// nextcharpos is 5 and previousDot is 4, then there is a period at position 4
						// and, since we're in this if statement, there is a hyphen character at
						// position 5. This means that the domain label we're parsing begins with a
						// hyphen, which is not allowed.
						///// (Note that previousDot starts with value -1.)
						if (previousDot == nextcharpos - 1)
							return new EmailIssue(nextcharpos + localpart.length() + 1,
									EmailIssue.Issue.DOMAIN_NAME_LABEL_STARTS_WITH_HYPHEN, localpart, domain);
					} else if (nextchar == '.')
						// We just hit (another) dot. We need to check for a few cases. Firstly, this
						// dot should not immediately follow the previous dot. If it does, we have an
						// issue (empty domain label). Second, this dot should not immediately follow a
						// hyphen. If it does, then we have a domain_name_label_ends_with_hyphen issue.
						// Then, we need to (1) take the current part we just parsed (everything AFTER
						// the previous dot up until, but NOT INCLUDING, this dot) and add it to the
						// parts list and (2) change the value of previousDot to be this dot.
						if (previousDot == nextcharpos - 1) {// Case 1: A domain name label begins with a dot (either
																// there is a dot at the beginning of the domain or
																// there is a dot immediately following another dot).
							// Examples offending emails:
							// pala@.arlith.com
							// pala@arlith..com
							// pala@pala.arlith.co..uk

							return new EmailIssue(nextcharpos + localpart.length() + 1,
									EmailIssue.Issue.DOMAIN_LABEL_BEGINS_WITH_DOT, localpart, domain);
						} else if (domain.charAt(nextcharpos - 1) == '-') {
							// Previous character was a hyphen. (charAt won't throw an
							// IndexOutOfBoundsException because we know we're past the first character due
							// to the first if.)
							return new EmailIssue(nextcharpos + localpart.length(),
									EmailIssue.Issue.DOMAIN_LABEL_ENDS_IN_HYPHEN, localpart, domain);
						} else {
							// Valid dot! Perform steps (1) and (2) mentioned above.
							// The substring method grabs the string from (including) the character AFTER
							// the previous dot, to (not including) the dot we just parsed.
							parts.add(domain.substring(previousDot + 1, previousDot = nextcharpos));
						}
					else if (nextchar == -1)
						// End of str reached. We're done parsing the domain.
						// A few things we have to check. We need to make sure that (1) the label we
						// just parsed did not end in a hyphen, then we need to make sure that
						// (2) we didn't just parse a dot (so that there is no empty label sitting at
						// the end of the domain).
						//
						// (We don't need to check that there was a label to parse, because we've
						// already checked to make sure that the domain string is not empty in an
						// else-if above.

						// First, check (2)
						if (previousDot == nextcharpos - 1)
							return new EmailIssue(EmailIssue.Issue.DOMAIN_ENDS_IN_DOT, localpart, domain);
						else if (domain.charAt(nextcharpos - 1) == '-')
							return new EmailIssue(EmailIssue.Issue.DOMAIN_LABEL_ENDS_IN_HYPHEN, localpart, domain);
						else {
							parts.add(domain.substring(previousDot + 1, nextcharpos));
							break;
						}
					else
						// Arbitrary, not-allowed character encountered.
						return new EmailIssue(nextcharpos, EmailIssue.Issue.ILLEGAL_CHARACTER_IN_DOMAIN, localpart,
								domain);
				}
				// Check total size:
				int total = parts.get(0).length();
				if (parts.size() < 2)
					return new EmailIssue(EmailIssue.Issue.DOMAIN_NAME_HAS_TOO_FEW_PARTS, localpart, domain);
				for (String s : parts)
					total += 1 + s.length();
				if (total > 253)
					return new EmailIssue(EmailIssue.Issue.DOMAIN_TOO_BIG, localpart, domain);
				return null;
			}
		}
	}

	public static Object checkPhoneNumberValidity(String phoneNumber) {
		// TODO Implement
		return null;
	}

	public static class EmailIssue {

		/**
		 * <p>
		 * Represents the type of issue that is encountered when parsing validating an
		 * email address. Each {@link EmailIssue} object has a type, which is
		 * represented by a value in this enumeration. That type is retrievable from
		 * {@link EmailIssue#getIssue()}.
		 * </p>
		 * <p>
		 * The meaning of the value returned by {@link EmailIssue#position()} is
		 * dependent on the value returned by {@link EmailIssue#getIssue()}. For some
		 * issues, such as {@link EmailIssue.Issue#ILLEGAL_CHARACTER}, the position
		 * represents the first occurrence of the specified issue. For other issues that
		 * occur at no conceptual position in the string (or where the position is
		 * obvious and need not be calculated), the {@link EmailIssue#position()} method
		 * will return <code>-1</code>.
		 * </p>
		 * <p>
		 * Additionally, the values of {@link EmailIssue#getLocalPart()} and
		 * {@link EmailIssue#getDomain()} represent the values of the respective parts
		 * of the provided email so long as the verifier algorithm was able to parse out
		 * all of such part. If the algorithm finds out that the email address is
		 * invalid halfway through parsing the local part,
		 * {@link EmailIssue#getLocalPart()} will return <code>null</code>, since there
		 * may be more characters in the local part that were not yet parsed. (Since it
		 * parses the local part first, {@link EmailIssue#getDomain()} would also be
		 * <code>null</code>.) If the algorithm finds the email to be invalid while
		 * parsing the domain, {@link EmailIssue#getLocalPart()} will return the local
		 * part that was parsed succesfully while {@link EmailIssue#getDomain()} will
		 * return <code>null</code> (since it may not have been fully parsed). Finally,
		 * if the algorithm encounters an error but knows what text, if any, makes up
		 * the local part and/or the domain, the respective methods will return values
		 * for those parts. For example, if {@link #EMPTY_EMAIL} is the case for an
		 * {@link EmailIssue} then {@link EmailIssue#getLocalPart()} and
		 * {@link EmailIssue#getDomain()} will both return the empty string, because the
		 * algorithm considers itself to have parsed over the whole provided "email."
		 * </p>
		 * 
		 * @author Palanath
		 *
		 */
		public enum Issue {
			/**
			 * <p>
			 * Indicates that the provided string is empty (and is therefore, trivially, not
			 * an email address).
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the empty string</li>
			 * <li>{@link EmailIssue#getDomain()} returns the empty string.</li>
			 * </ul>
			 */
			EMPTY_EMAIL,
			/**
			 * <p>
			 * Indicates that the provided email address began with a double quotation mark
			 * (<code>"</code>) but did not have a closing double quotation mark. A closing
			 * double quotation mark is a double quotation mark that is not "escaped" (by
			 * having an escaping backslash in front).
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the whole, provided
			 * string.</li>
			 * <li>{@link EmailIssue#getDomain()} returns the empty string.</li>
			 * </ul>
			 */
			NONTERMINATED_QUOTED_LOCALPART,
			/**
			 * <p>
			 * Indicates that the end of the string was found before an <code>@</code>
			 * symbol, that separates the local part from the domain, was found. This occurs
			 * when the end of the string is reached while parsing the local-part before an
			 * <code>@</code> sign is found.
			 * </p>
			 * <p>
			 * Since the local part is parsed before the <code>@</code> symbol is
			 * encountered, this issue will only ever be raised if
			 * {@link #NONTERMINATED_QUOTED_LOCALPART} is <b>not</b> raised. I.e., if the
			 * local part is quoted (the string begins with a quotation mark) and the end of
			 * the string is reached before there's a closing quotation mark,
			 * {@link #NONTERMINATED_QUOTED_LOCALPART}. Otherwise, if there <b>is</b> a
			 * closing quotation mark but the string ends immediately after, this issue is
			 * raised instead.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the whole, provided
			 * string.</li>
			 * <li>{@link EmailIssue#getDomain()} returns the empty string.</li>
			 * </ul>
			 */
			END_OF_STRING_FOUND_BEFORE_AT,
			/**
			 * <p>
			 * Indicates that there was not an <code>@</code> symbol immediately following
			 * the local part. This occurs when the string has another character after the
			 * local-part, but that character is not the <code>@</code> symbol. This can
			 * only happen when a quoted local-part is used, e.g.:
			 * </p>
			 * 
			 * <pre>
			 * <code>"john.doe"123@arlith.com</code>
			 * </pre>
			 * 
			 * <p>
			 * In such a case, the <code>1</code> character after the second
			 * double-quotation mark would be in offense and would cause this type to be the
			 * issue.
			 * </p>
			 * <p>
			 * If this issue is encountered, {@link EmailIssue#position()} will return the
			 * position of the character after the local-part that should be an
			 * <code>@</code>.
			 * </p>
			 */
			LOCAL_PART_NOT_FOLLOWED_BY_AT,
			/**
			 * <p>
			 * Indicates that, while parsing a quoted local part, an escaping backslash was
			 * found in front of a character that does not need to be escaped. This issue is
			 * only raised if {@link Utilities#checkEmailValidity(String, boolean)} is
			 * called with <code>checkArbitraryEscapes</code> enabled (i.e., the second
			 * argument provided is <code>true</code>).
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue}, then:
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the character that
			 * was arbitrarily escaped (this is the character following the backslash).</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns <code>null</code>. (This is
			 * because it might not be the case that the whole of the local part was
			 * successfully parsed; there may be remaining characters in the local part that
			 * are not yet parsed.)</li>
			 * <li>{@link EmailIssue#getDomain()} returns <code>null</code>.</li>
			 * </ul>
			 */
			ARBITRARY_ESCAPE,
			/**
			 * Indicates that the local part in the email begins with a period. The local
			 * part should either be quoted (in which case it ends with a double quotation
			 * mark) or start with a valid <i>text character</i>. Although it can contain
			 * periods, those periods must each be surrounded with text characters (at least
			 * one directly adjacent to the period on either side).
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the string
			 * <code>"."</code>.</li>
			 * <li>{@link EmailIssue#getDomain()} returns the <code>null</code>.</li>
			 * </ul>
			 */
			LOCAL_PART_BEGINS_WITH_PERIOD,
			/**
			 * Indicates that the local part in the email ends in a period. The local part
			 * should either be quoted (in which case it ends with a double quotation mark)
			 * or end with a valid <i>text character</i>. Although it can contain periods,
			 * those periods must each be surrounded with text characters (at least one
			 * directly adjacent to the period on either side).
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the offending
			 * period.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part,
			 * including the offending period.</li>
			 * <li>{@link EmailIssue#getDomain()} returns <code>null</code>.</li>
			 * </ul>
			 */
			LOCAL_PART_ENDS_WITH_PERIOD,
			/**
			 * Indicates that the local part of the email address is not quoted but contains
			 * at least two periods that are adjacent to each other. Periods must be
			 * surrounded with text characters (at least one text character directly
			 * adjacent to each period on either side). If this value is the
			 * {@link EmailIssue#getIssue() issue of an EmailIssue}, then that
			 * {@link EmailIssue#position() EmailIssue's position()} method will return the
			 * position of the first (leftmost) of the (first) two adjacent periods in the
			 * string.
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the location of the first of the
			 * two touching dots encountered.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns <code>null</code>.</li>
			 * <li>{@link EmailIssue#getDomain()} returns <code>null</code>.</li>
			 * </ul>
			 */
			TOUCHING_DOTS_IN_LOCAL_PART,
			/**
			 * Indicates that a character was encountered that should not exist in the email
			 * address. If this value is the {@link EmailIssue#getIssue() issue of an
			 * EmailIssue}, then that {@link EmailIssue#position() EmailIssue's position()}
			 * method will return the position of the illegal character.
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the illegal
			 * character.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns <code>null</code>.</li>
			 * <li>{@link EmailIssue#getDomain()} returns <code>null</code>.</li>
			 * </ul>
			 */
			ILLEGAL_CHARACTER,
			/**
			 * <p>
			 * Indicates that there were too many octets inside a label in the domain name.
			 * This happens when the domain name is not an IP address.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the 64th character
			 * in the offending domain name label. Note that domain name labels can be a
			 * maximum of 63 characters in length. (The position is indexed from the
			 * beginning of the provided email string, so the first character of the local
			 * part of the provided email is at index 0.)</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			TOO_MANY_OCTETS_IN_DOMAIN_NAME_LABEL,
			/**
			 * <p>
			 * Indicates that a label in a domain name starts with a hyphen.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the offending
			 * hyphen character in the offending domain name label. (The position is the
			 * position of the hyphen indexed from the beginning of the provided email
			 * string, so the first character of the local part of the provided email is at
			 * index 0.)</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_NAME_LABEL_STARTS_WITH_HYPHEN,
			/**
			 * <p>
			 * Indicates that one of the labels in the domain was found to begin with a dot.
			 * This problem occurs as one instance of two cases, either:
			 * </p>
			 * <ul>
			 * <li>A dot is encountered at the very beginning of the domain, e.g. in
			 * 
			 * <pre>
			 * <code>pala@.arlith.org</code>
			 * </pre>
			 * 
			 * or,</li>
			 * <li>A dot is encountered immediately after another dot, as in:
			 * 
			 * <pre>
			 * <code>pala@arlith..org</code>
			 * </pre>
			 * 
			 * </li>
			 * </ul>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>If the domain itself begins with a dot,
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the dot (at the
			 * beginning of the domain),</li>
			 * <li><i>otherwise</i>, if the domain itself does not begin with a dot,
			 * {@link EmailIssue#position()} returns the position of the dot at the
			 * beginning of the offending domain label, (i.e., the <i>second</i> of the two
			 * touching dots).</li>
			 * </ul>
			 * (The position is indexed from the beginning of the provided email string, so
			 * the first character of the local part of the provided email is at index 0.)
			 * </li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_LABEL_BEGINS_WITH_DOT,
			/**
			 * <p>
			 * Indicates that one of the labels in the domain was found to end in a hyphen.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the offending
			 * hyphen character in the offending domain name label. (The position is the
			 * position of the hyphen indexed from the beginning of the provided email
			 * string, so the first character of the local part of the provided email is at
			 * index 0.)</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_LABEL_ENDS_IN_HYPHEN,
			/**
			 * <p>
			 * Indicates that an illegal character was found in the domain of the provided
			 * email.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the illegal
			 * character. (The position of the character is indexed from the beginning of
			 * the provided email string, so the first character of the local part of the
			 * provided email is at index 0.)</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			ILLEGAL_CHARACTER_IN_DOMAIN,
			/**
			 * <p>
			 * Indicates that the domain of the email ended in a dot. The position of the
			 * dot is the very end of the string and is the very end of the value returned
			 * by {@link EmailIssue#getDomain()}.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_ENDS_IN_DOT,
			/**
			 * <p>
			 * Indicates that the domain of the email is too large. The domain of an email
			 * is restricted to being 253 characters in length at max.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_TOO_BIG,
			/**
			 * <p>
			 * Indicates that the domain of the email string provided is empty. This occurs
			 * when the provided string ends in the <code>@</code> symbol that immediately
			 * follows the local-part.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns the empty string.</li>
			 * </ul>
			 */
			DOMAIN_EMPTY,
			/**
			 * <p>
			 * Indicates that the domain part of the email denotes an IP address, but
			 * there's no closing bracket at the end of the IP address. If this issue is
			 * raised, the algorithm has determined that the domain is an IP address by its
			 * beginning with an opening bracket. The closing bracket should have been found
			 * at the end of the string.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns the empty string.</li>
			 * </ul>
			 */
			DOMAIN_HAS_NO_CLOSING_BRACKET,
			/**
			 * <p>
			 * Indicates that an IPv4 address was being used as the domain, but the IP
			 * address does not have the correct number of octets. Each IPv4 address should
			 * have exactly 4 octets, each separated by a period.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_IPV4_HAS_INVALID_NUMBER_OF_OCTETS,
			/**
			 * <p>
			 * Indicates that the domain of the email address used brackets, but did not
			 * seem to be an IP address (specifically, it did not contain periods).
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the first character
			 * after the opening bracket for what should be the IP address.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_USED_BRACKETS_BUT_INVALID_IP,
			/**
			 * <p>
			 * Indicates that the domain of the email address used brackets, but that those
			 * brackets did not contain any characters between them.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the opening bracket
			 * in the domain portion of the email.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_USED_BRACKETS_BUT_NO_IP,
			/**
			 * <p>
			 * Indicates that one of the octets of the IPv4 address is not valid. This can
			 * either be because it has too many characters (as the second octet in
			 * <code>192.00132.4.0</code>), is empty (as third octet in
			 * <code>192.132..0</code>), is composed of characters that are not decimal
			 * numbers (as the second octet in <code>192.abc.4.0</code>), or is for some
			 * other reason, is malformed.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns the position of the first character
			 * of the octet, or the character where it was expected (if it is empty).</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_IPV4_INVALID_OCTET,
			/**
			 * <p>
			 * Indicates that the domain name provided has too few labels, (specifically
			 * that it has fewer than 2 labels).
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			DOMAIN_NAME_HAS_TOO_FEW_PARTS,
			/**
			 * <p>
			 * Indicates that an IPv6 address was used for the domain. IPv6 is not currently
			 * supported by this implementation.
			 * </p>
			 * <p>
			 * If this issue is the case of an {@link EmailIssue},
			 * </p>
			 * <ul>
			 * <li>{@link EmailIssue#position()} returns -1.</li>
			 * <li>{@link EmailIssue#getLocalPart()} returns the parsed local part.</li>
			 * <li>{@link EmailIssue#getDomain()} returns everything after the
			 * <code>@</code> symbol which follows the parsed local part.</li>
			 * </ul>
			 */
			IPV6_USED;
		}

		private final int pos;
		private final Issue issue;
		private final String localPart, domain;

		private EmailIssue(Issue issue, String localPart, String domain) {
			this(-1, issue, localPart, domain);
		}

		private EmailIssue(int pos, Issue issue, String localPart, String domain) {
			this.pos = pos;
			this.issue = issue;
			this.localPart = localPart;
			this.domain = domain;
		}

		public Issue getIssue() {
			return issue;
		}

		public int position() {
			return pos;
		}

		public String getLocalPart() {
			return localPart;
		}

		public String getDomain() {
			return domain;
		}

	}

	public static class UsernameIssue {
		private final int charpos;
		private final Issue issue;

		public Issue getIssue() {
			return issue;
		}

		public int getCharpos() {
			return charpos;
		}

		private UsernameIssue(int charpos, Issue issue) {
			this.charpos = charpos;
			this.issue = issue;
		}

		public enum Issue {
			CONTAINED_ILLEGAL_CHARACTER, USERNAME_TOO_SHORT, USERNAME_TOO_LONG;
		}
	}

	public static boolean isValidPhoneNumber_Jan_2021(String number) {
		return number.length() >= 10 && (number.charAt(0) == '+' && StringTools.isNumeric(number.substring(1))
				|| StringTools.isNumeric(number));
	}

	public static boolean isValidPhoneNumber(String number) {
		return isValidPhoneNumber_Jan_2021(number);
	}

	public static boolean isValidPassword(String password) {
		return true;// Any password is okay because the server will receive a similar string
					// regardless due to hashing.
	}

	public static boolean isValidUsername(String username) {
		return username.length() > 2 && !StringTools.containsIgnoreCase(username, controlCharacters)
				&& username.length() < 20;
	}

	public static UserReference isValidUsernameReference(String username) {
		int htpos = username.indexOf('#');
		if (htpos == -1)
			return null;
		String disc = username.substring(htpos + 1, username.length());
		return htpos > 0 && htpos < username.length() - 3 && isValidUsername(username.substring(0, htpos))
				&& StringTools.isNumeric(disc) ? new UserReference(username.substring(0, htpos), disc) : null;
	}

	public static boolean isValidEmail(String email) {
		// TODO Implement
//		CharacterParser cp = CharacterStream.from(email);
//
//		// Parse local-part:
//		int c = cp.next();
//		if (c == -1)
//			return false;
//		if (c == '"') {
//			// TODO Parse quoted local-part.
//		} else {
//			// Parse unquoted local-part.
//			switch (c) {
//			
//			}
//		}
		return email.contains("@") && email.length() < 321;
	}

	public static int getPreferredPort() {
		return preferredPort;
	}

	public static void setPreferredPort(int preferredPort) {
		Utilities.preferredPort = preferredPort;
	}

	public static String getPreferredDestinationAddress() {
		return preferredDestinationAddress;
	}

	public static void setPreferredDestinationAddress(String preferredDestinationAddress) {
		Utilities.preferredDestinationAddress = preferredDestinationAddress;
	}

	public static class UserReference {
		private final String username, disc;

		public UserReference(String username, String disc) {
			this.username = username;
			this.disc = disc;
		}

		public String getUsername() {
			return username;
		}

		public String getDisc() {
			return disc;
		}

		@Override
		public String toString() {
			return getUsername() + '#' + getDisc();
		}

	}
}
