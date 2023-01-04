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

/**
 * This class represents the log in scene for the client GUI frontend. This
 * class instantiates the
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
		presentation.lockUIForLoggingIn();
		Thread t = new Thread(() -> {
			try {
				String un = presentation.getUsername(), pw = presentation.getPassword();
				ArlithFrontend.getGuiLogger().dbg("Log in with username=" + un + ", password=" + pw);

				builder.setUsername(un);
				builder.setPassword(pw);
				ArlithClient client;
				try {
					client = builder.login();
				} catch (LoginError e) {
					presentation.showLoginProblem(e.getLoginError());
				} catch (LoginFailureException | MalformedServerResponseException e) {
					ArlithFrontend.getGuiLogger().err(e);
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
			}
		});
		t.start();
	}

}
