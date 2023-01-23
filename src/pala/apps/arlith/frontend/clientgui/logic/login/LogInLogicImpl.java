package pala.apps.arlith.frontend.clientgui.logic.login;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.client.MalformedServerResponseException;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentationWithLiveInputResponse.Issue;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.Utilities.EmailIssue;
import pala.apps.arlith.libraries.Utilities.UsernameIssue;

/**
 * This class represents the logic for the initial scene shown to the user of
 * the Client GUI frontend.
 * 
 * @author Palanath
 *
 */
public class LogInLogicImpl implements LogInLogic {
	private final ClientGUIFrontend frontend;
	private final ArlithClientBuilder builder;
	private LogInPresentation presentation;

	public LogInLogicImpl(ClientGUIFrontend frontend, ArlithClientBuilder builder) {
		this.frontend = frontend;
		this.builder = builder;
	}

	@Override
	public void hook(LogInPresentation presentation) {
		this.presentation = presentation;
	}

	@Override
	public void triggerLogIn() {
		ArlithFrontend.getGuiLogger().dbg("Attempting to log in...");
		presentation.lockUIForLoggingIn();
		ArlithFrontend.getGuiLogger().dbg("(1) GUI Locked");
		Thread t = new Thread(() -> {
			try {
				String un = presentation.getLogInIdentifier(), pw = presentation.getPassword();
				ArlithFrontend.getGuiLogger().dbg("(2) Using Identifier: " + un);

				builder.setUsername(un);
				builder.setPassword(pw);
				ArlithClient client;
				try {
					client = builder.login();
				} catch (LoginError e) {
					presentation.showLoginProblem(e.getLoginError());
					ArlithFrontend.getGuiLogger().dbg("(E) Encountered log in error: " + e.getLoginError());
					return;
				} catch (LoginFailureException e) {
					presentation.showLogInFailure(e);
					return;
				} catch (MalformedServerResponseException e) {
					ArlithFrontend.getGuiLogger().dbg("(E) Encountered log in error; " + e.getLocalizedMessage());
					ArlithFrontend.getGuiLogger().err(e);
					return;
				}

//				Platform.runLater(new Runnable() {
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						// Upon successful log in, disable the presentation and show the next scene.
//						frontend.setClient(client);
//						presentation.hide();
//						new WhateverNextSceneWillBeCalled(frontend).show();
//					}
//				});
			} finally {
				// Once the above Platform.runLater(...) call is uncommented, the following line
				// will need to be reconsidered.
				presentation.unlockUIForLoggingIn();
				ArlithFrontend.getGuiLogger().dbg("(L) Unlocking GUI...");
			}
		});
		t.start();
	}

	@Override
	public void triggerCreateAccount() {
		ArlithFrontend.getGuiLogger().dbg("Attempting to create an account...");
		ArlithFrontend.getGuiLogger().dbg("(1) Using username: " + presentation.getUsername());
	}

	@Override
	public void triggerCheckUsername() {
		LogInPresentationWithLiveInputResponse presentation = (LogInPresentationWithLiveInputResponse) this.presentation;
		String username = presentation.getUsername();
		UsernameIssue issue = Utilities.checkUsernameValidity(username);
		if (issue == null)
			// Make pres show "email is correct"
			presentation.showUsernameError(null);
		else
			switch (issue.getIssue()) {
			case CONTAINED_ILLEGAL_CHARACTER:
				presentation.showUsernameError(new Issue(LogInPresentationWithLiveInputResponse.Severity.ERROR,
						"\"" + username.charAt(issue.getCharpos()) + "\" not allowed", 0));
				break;
			case USERNAME_TOO_LONG:
				presentation.showUsernameError(
						new Issue(LogInPresentationWithLiveInputResponse.Severity.ERROR, "Username too long!", -1));
				break;
			case USERNAME_TOO_SHORT:
				presentation.showUsernameError(
						new Issue(LogInPresentationWithLiveInputResponse.Severity.ERROR, "Username too short!", -1));
				break;
			}
	}

	@Override
	public void triggerCheckLogInIdentifier() {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerCheckPassword() {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerCheckEmail() {
		LogInPresentationWithLiveInputResponse presentation = (LogInPresentationWithLiveInputResponse) this.presentation;
		String email = presentation.getEmail();
		EmailIssue validationIssue = Utilities.checkEmailValidity(email);
		if (validationIssue == null)
			presentation.showEmailError(null);
		else {
			String message;
			switch (validationIssue.getIssue()) {
			case ARBITRARY_ESCAPE:
				presentation.showEmailError(new Issue(LogInPresentationWithLiveInputResponse.Severity.WARNING,
						"Unnecessary escape character (\"\\\")", 0));
				return;
			case DOMAIN_EMPTY:
				message = "Domain cannot be empty";
				break;
			case DOMAIN_ENDS_IN_DOT:
				message = "Domain not finished";
				break;
			case DOMAIN_HAS_NO_CLOSING_BRACKET:
				message = "IP address missing closing bracket ([)";
				break;
			case DOMAIN_IPV4_HAS_INVALID_NUMBER_OF_OCTETS:
				message = "IPv4 invalid # of octets";
				break;
			case DOMAIN_IPV4_INVALID_OCTET:
				message = "IPv4 invalid octet";
				break;
			case DOMAIN_LABEL_BEGINS_WITH_DOT:
				message = "Domain label starts with period (.)";
				break;
			case DOMAIN_LABEL_ENDS_IN_HYPHEN:
				message = "Domain label ends in period (.)";
				break;
			case DOMAIN_NAME_HAS_TOO_FEW_PARTS:
				message = "Domain has too few parts (labels)";
				break;
			case DOMAIN_NAME_LABEL_STARTS_WITH_HYPHEN:
				message = "Domain label can't start with hyphen (-)";
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
				message = "Email contains illegal character";
				break;
			case ILLEGAL_CHARACTER_IN_DOMAIN:
				message = "Domain contains illegal character";
				break;
			case IPV6_USED:
				message = "IPv6 emails not yet supported";
				break;
			case LOCAL_PART_BEGINS_WITH_PERIOD:
				message = "Local part can't start with period (.)";
				break;
			case LOCAL_PART_ENDS_WITH_PERIOD:
				message = "Local part can't end with period (.)";
				break;
			case LOCAL_PART_NOT_FOLLOWED_BY_AT:
				message = "Local part not followed by @ symbol";
				break;
			case NONTERMINATED_QUOTED_LOCALPART:
				message = "Local part quote not balanced";
				break;
			case TOO_MANY_OCTETS_IN_DOMAIN_NAME_LABEL:
				message = "Domain label has more than 63 characters";
			case TOUCHING_DOTS_IN_LOCAL_PART:
				message = "Local part dots cannot touch";
				break;
			default:
				message = "Email not valid";
			}
			presentation.showEmailError(new Issue(LogInPresentationWithLiveInputResponse.Severity.ERROR, message,
					validationIssue.position()));
		}
	}

	@Override
	public void triggerCheckPhoneNumber() {
		// TODO Auto-generated method stub

	}

}
