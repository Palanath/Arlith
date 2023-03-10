package pala.apps.arlith.frontend.clientgui.logic.login;

import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.client.MalformedServerResponseException;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;
import pala.apps.arlith.frontend.clientgui.logic.home.HomeScene;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation.Input;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Severity;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.Utilities.EmailIssue;
import pala.apps.arlith.libraries.Utilities.PhoneNumberIssue;
import pala.apps.arlith.libraries.Utilities.UsernameIssue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

/**
 * This class represents the logic for the initial scene shown to the user of
 * the Client GUI frontend.
 * 
 * @author Palanath
 *
 */
public class LogInScene extends ClientGUIFrontend.UserInterface implements LogInLogic {
	private static Issue determineEmailIssue(String email) {
		EmailIssue validationIssue = Utilities.checkEmailValidity(email);
		if (validationIssue == null)
			return null;
		else {
			String message;
			switch (validationIssue.getIssue()) {
			// Showing this as only a warning allows the user to submit broken emails, as a
			// more serious error may occur after the illegal character but will not be
			// reported.
//			case ARBITRARY_ESCAPE:
//				presentation.showEmailError(new Issue(Severity.WARNING,
//						"Unnecessary escape character (\"\\\")", 0));
//				return;
			case DOMAIN_EMPTY:
				message = "Domain cannot be empty";
				break;
			case DOMAIN_ENDS_IN_DOT:
				message = "Domain not finished";
				break;
			case IP_DOES_NOT_END_IN_CLOSING_BRACKET:
				message = "IP doesn't end in ']' character.";
				break;
			case DOMAIN_IPV4_HAS_INVALID_NUMBER_OF_OCTETS:
				message = "IPv4, invalid # of octets";
				break;
			case DOMAIN_IPV4_INVALID_OCTET:
				message = "Invalid IPv4 octet";
				break;
			case DOMAIN_LABEL_BEGINS_WITH_DOT:
				message = "Domain label starts w/ period (.)";
				break;
			case DOMAIN_LABEL_ENDS_IN_HYPHEN:
				message = "Domain label ends in period (.)";
				break;
			case DOMAIN_NAME_HAS_TOO_FEW_PARTS:
				message = "Domain has too few parts (labels)";
				break;
			case DOMAIN_NAME_LABEL_STARTS_WITH_HYPHEN:
				message = "Domain label starts w/ hyphen (-)";
				break;
			case DOMAIN_TOO_BIG:
				message = "Domain too large";
				break;
			case DOMAIN_USED_BRACKETS_BUT_INVALID_IP:
				message = "Invalid IP address";
				break;
			case DOMAIN_USED_BRACKETS_BUT_NO_IP:
				message = "No IP address specified";
				break;
			case EMPTY_EMAIL:
				message = "Please enter something";
				break;
			case END_OF_STRING_FOUND_BEFORE_AT:
				message = "No @ symbol after local part";
				break;
			case ILLEGAL_CHARACTER:
				message = "Email has illegal character";
				break;
			case ILLEGAL_CHARACTER_IN_DOMAIN:
				message = "Domain has illegal character";
				break;
			case IPV6_USED:
				message = "IPv6 emails not yet supported";
				break;
			case LOCAL_PART_BEGINS_WITH_PERIOD:
				message = "Local part starts w/ period (.)";
				break;
			case LOCAL_PART_ENDS_WITH_PERIOD:
				message = "Local part ends w/ period (.)";
				break;
			case LOCAL_PART_NOT_FOLLOWED_BY_AT:
				message = "Local part not followed by @";
				break;
			case NONTERMINATED_QUOTED_LOCALPART:
				message = "Local part quote not balanced";
				break;
			case TOO_MANY_OCTETS_IN_DOMAIN_NAME_LABEL:
				message = "Domain label too big (>63)";
				break;
			case TOUCHING_DOTS_IN_LOCAL_PART:
				message = "Local part dots cannot touch";
				break;
			default:
				message = "Email not valid";
			}
			return new Issue(Severity.ERROR, message, validationIssue.position());
		}
	}

	private static Issue determinePhoneNumberIssue(String phoneNumber) {
		PhoneNumberIssue issue = Utilities.checkPhoneNumberValidity(phoneNumber);
		if (issue == null)
			return null;
		else {
			String message;
			switch (issue.getIssue()) {
			case EMPTY_INPUT:
				return null;
			case MISPLACED_PLUS_SYMBOL:
				message = "'+' only allowed at beginning.";
				break;
			case NON_DIGIT_WHERE_DIGIT_EXPECTED:
				message = "'" + phoneNumber.charAt(issue.getCharpos()) + "' is not a digit.";
				break;
			case NOTHING_AFTER_PLUS:
				message = "No number provided after '+'.";
				break;
			case PHONE_NUMBER_TOO_LONG:
				message = "Phone # too long (max 15 digits).";
				break;
			case PHONE_NUMBER_TOO_SHORT:
				message = "Phone # too short (min 10 digits).";
				break;
			default:
				message = "Phone # invalid.";
				break;
			}
			return new Issue(Severity.ERROR, message, issue.getCharpos());
		}
	}

	private static Issue determineUsernameIssue(String username) {
		UsernameIssue issue = Utilities.checkUsernameValidity(username);
		if (issue == null)
			// Make pres show "email is correct"
			return null;
		else
			switch (issue.getIssue()) {
			case CONTAINED_ILLEGAL_CHARACTER:
				return new Issue(Severity.ERROR, "\"" + username.charAt(issue.getCharpos()) + "\" not allowed",
						issue.getCharpos());
			case USERNAME_TOO_LONG:
				return new Issue(Severity.ERROR, "Username too long!", -1);
			case USERNAME_TOO_SHORT:
				return new Issue(Severity.ERROR, "Username too short!", -1);
			}
		return new Issue(Severity.ERROR, "Invalid username.", -1);
	}

	private final ArlithClientBuilder builder;
	private LogInPresentation presentation;

	public LogInScene(ClientGUIFrontend frontend, ArlithClientBuilder builder) {
		frontend.super();
		this.builder = builder;
	}

	@Override
	public void triggerCheckInput(Input input) {
		LogInPresentationWithLiveInputResponse presentation = (LogInPresentationWithLiveInputResponse) this.presentation;
		String inputString = presentation.getInputValue(input);
		switch (input) {
		case EMAIL_ADDRESS:
			Issue issue = determineEmailIssue(inputString);
			if (issue != null)
				presentation.showInputError(issue, Input.EMAIL_ADDRESS);
			else
				presentation.showInputValid(Input.EMAIL_ADDRESS);
			break;
		case LOGIN_IDENTIFIER:
			// This block contains clauses that check the syntax for specific types of
			// identifiers (email, phone #, and user tag). If the input cannot be determined
			// to be one of those types, we break out of the block.
			HANDLE_SYNTAX_FOR_DETERMINED_TYPES: {
				// The log in identifier can be either an email address, a phone number, or a
				// user tag. This gives us 3 cases:
				//
				// The email address MUST have an @ symbol.
				// The user tag CANNOT have an @ symbol but MUST have a #.
				// The phone number CANNOT have an @ symbol and CANNOT have a #.
				if (inputString.contains("@")) {// Email
					issue = determineEmailIssue(inputString);
					if (issue != null)
						presentation.showInputError(issue, Input.LOGIN_IDENTIFIER);
					else
						presentation.showInputValid(Input.LOGIN_IDENTIFIER);
				} else if (inputString.contains("#")) {// Tag
					int hashind = inputString.indexOf('#');
					if (hashind == inputString.length() - 1)
						presentation.showInputError(new Issue(Severity.ERROR, "Tag can't end in '#'.", -1),
								Input.LOGIN_IDENTIFIER);
					else {
						String username = inputString.substring(0, hashind);
						String disc = inputString.substring(hashind + 1);
						issue = determineUsernameIssue(username);
						if (issue != null)
							presentation.showInputError(issue, Input.LOGIN_IDENTIFIER);
						else {// Verify disc.
							for (int i = 0; i < disc.length(); i++)
								if (!Character.isDigit(disc.charAt(i))) {
									presentation.showInputError(
											new Issue(Severity.ERROR, "'" + disc.charAt(i) + "' not allowed after '#'.",
													username.length() + 1 + i),
											Input.LOGIN_IDENTIFIER);
									return;
								}
							if (disc.length() < 4)
								presentation.showInputError(new Issue(Severity.ERROR, "Discriminator too short.", -1),
										Input.LOGIN_IDENTIFIER);
							else
								presentation.showInputValid(Input.LOGIN_IDENTIFIER);
						}
					}
				} else// Treat as phone number if first character matches what a phone # could start
						// with. Otherwise, consider type to be unknown.
				if (inputString.isEmpty())
					break HANDLE_SYNTAX_FOR_DETERMINED_TYPES;
				else {
					char first = inputString.charAt(0);
					if (first != '+' && !Character.isDigit(first))
						break HANDLE_SYNTAX_FOR_DETERMINED_TYPES;
					issue = determinePhoneNumberIssue(inputString);
					if (issue != null)
						presentation.showInputError(issue, Input.LOGIN_IDENTIFIER);
					else
						presentation.showInputValid(Input.LOGIN_IDENTIFIER);
				}
				return;
			}
			// We get here if the type of identifier the user is using has not been
			// determined.
			presentation.showInputError(new Issue(Severity.ERROR, "Tag, email, or phone # required.", -1),
					Input.LOGIN_IDENTIFIER);
			break;
		case PASSWORD:
			if (inputString.isEmpty())
				presentation.showInputError(new Issue(Severity.ERROR, "Password can't be empty", -1), Input.PASSWORD);
			else if (inputString.length() < 9)
				presentation.showInputError(new Issue(Severity.WARNING, "Short password", -1), Input.PASSWORD);
			else
				presentation.showInputValid(Input.PASSWORD);
			break;
		case PHONE_NUMBER:
			issue = determinePhoneNumberIssue(inputString);
			if (issue != null)
				presentation.showInputError(issue, Input.PHONE_NUMBER);
			else
				presentation.showInputValid(Input.PHONE_NUMBER);
			break;
		case USERNAME:
			issue = determineUsernameIssue(inputString);
			if (issue != null)
				presentation.showInputError(issue, Input.USERNAME);
			else
				presentation.showInputValid(Input.USERNAME);
		}
	}

	@Override
	public void triggerCreateAccount() {
		ArlithFrontend.getGuiLogger().dbg("Attempting to create an account...");
		ArlithFrontend.getGuiLogger().dbg("(1) Using username: " + presentation.getInputValue(Input.USERNAME));
	}

	@Override
	public void triggerLogIn() {
		ArlithFrontend.getGuiLogger().dbg("Attempting to log in...");
		presentation.lockUI();
		ArlithFrontend.getGuiLogger().dbg("(1) GUI Locked");
		Thread t = new Thread(() -> {
			String un = presentation.getInputValue(Input.LOGIN_IDENTIFIER),
					pw = presentation.getInputValue(Input.PASSWORD);
			ArlithFrontend.getGuiLogger().dbg("(2) Using Identifier: " + un);

			// Distinguish identifier between one of tag, email, and phone#.
			if (un.contains("@"))
				builder.setEmail(un);
			else if (un.contains("#"))
				builder.setUsername(un);
			else
				builder.setPhoneNumber(un);

			builder.setPassword(pw);
			ArlithClient client;
			try {
				client = builder.login();
			} catch (LoginError e) {
				presentation.showLoginProblem(e.getLoginError());
				ArlithFrontend.getGuiLogger().dbg("(E) Encountered log in error: " + e.getLoginError());
				presentation.unlockUI();
				ArlithFrontend.getGuiLogger().dbg("(L) Unlocking GUI...");
				return;
			} catch (LoginFailureException e) {
				presentation.showLogInFailure(e);
				presentation.unlockUI();
				ArlithFrontend.getGuiLogger().dbg("(L) Unlocking GUI...");
				return;
			} catch (MalformedServerResponseException e) {
				ArlithFrontend.getGuiLogger().dbg("(E) Encountered log in error; " + e.getLocalizedMessage());
				ArlithFrontend.getGuiLogger().err(e);
				presentation.unlockUI();
				ArlithFrontend.getGuiLogger().dbg("(L) Unlocking GUI...");
				return;
			} catch (BlockException | UnknownCommStateException e) {
				ArlithFrontend.getGuiLogger().err(e);
				presentation.unlockUI();
				return;
			} catch (Throwable e) {
				presentation.unlockUI();
				throw e;
			} finally {
				// Reset builder.
				builder.setEmail(null).setUsername(null).setPhoneNumber(null);
			}

			getFrontend().setClient(client);
			// Attempt to show the home window.
			try {
				new HomeScene(getFrontend()).display();
			} catch (WindowLoadFailureException e) {
				e.printStackTrace();
			}
		});
		t.start();
	}

	@Override
	protected void show(Stage stage) throws WindowLoadFailureException {
		presentation = loadPresentation();
		presentation.show(stage);
	}

}
