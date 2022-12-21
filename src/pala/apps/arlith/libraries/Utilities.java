package pala.apps.arlith.libraries;

import java.util.Arrays;

import pala.libs.generic.strings.StringTools;

/**
 * General {@link Utilities}. This class shouldn't depend on any libraries
 * besides the standard library. See {@link JavaFXUtilities} for JFX utilities.
 * 
 * @author Palanath
 *
 */
public class Utilities {
	public static final String DEFAULT_DESTINATION_ADDRESS = "arlith.net";
	public static final int DEFAULT_PORT = 42069;

	private static int preferredPort = DEFAULT_PORT;
	private static String preferredDestinationAddress = DEFAULT_DESTINATION_ADDRESS;

	private static String[] controlCharacters = { "#", "@", "<", ">" };

	public static String[] getControlCharacters() {
		return Arrays.copyOf(controlCharacters, controlCharacters.length);
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
