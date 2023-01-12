package pala.apps.arlith.frontend.clientgui.logic.login;

import javafx.application.Platform;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.client.MalformedServerResponseException;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;

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
				} catch (LoginFailureException | MalformedServerResponseException e) {
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

}
