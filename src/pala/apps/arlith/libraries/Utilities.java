package pala.apps.arlith.libraries;

import java.time.LocalDateTime;
import java.util.Arrays;

import pala.apps.arlith.libraries.Utilities.UsernameIssue.Issue;
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
	 * {@link Issue#TOO_SHORT};</li>
	 * <li>Otherwise, if the username is more than <code>20</code> characters in
	 * length, this method returns a {@link UsernameIssue} indicative of
	 * {@link Issue#TOO_LONG}.</li>
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
			return new UsernameIssue(-1, Issue.TOO_SHORT);
		else if (username.length() > 20)
			return new UsernameIssue(-1, Issue.TOO_LONG);
		else
			for (int i = 0, j; i < username.length(); i++)
				if ((j = JavaTools.indexOf(String.valueOf(username.charAt(i)), getControlCharacters())) != -1)
					return new UsernameIssue(j, Issue.CONTAINED_ILLEGAL_CHARACTER);
		return null;

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
			CONTAINED_ILLEGAL_CHARACTER, TOO_SHORT, TOO_LONG;
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
