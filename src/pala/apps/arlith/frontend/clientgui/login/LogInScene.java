package pala.apps.arlith.frontend.clientgui.login;

import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.backend.client.LoginFailureException;
import pala.apps.arlith.backend.client.MalformedServerResponseException;
import pala.apps.arlith.backend.common.protocol.errors.LoginError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;
import pala.apps.arlith.libraries.frontends.FrontendScene;
import pala.apps.arlith.libraries.frontends.interfacing.Theme;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

/**
 * This class represents the log in scene for the client GUI frontend. This
 * class instantiates the
 * 
 * @author Palanath
 *
 */
public class LogInScene implements FrontendScene<ClientGUIFrontend>, LogInLogic {
	private final ClientGUIFrontend frontend;
	private final ArlithClientBuilder builder;
	private LogInPresentation presentation;

	public LogInScene(ClientGUIFrontend frontend, ArlithClientBuilder builder) {
		this.frontend = frontend;
		this.builder = builder;
	}

	@Override
	public ClientGUIFrontend getFrontend() {
		return frontend;
	}

	@Override
	public void show() throws SceneShowFailureException {
		// The logic class has already been instantiated. Instantiate a presentation for
		// the scene (by querying the theme(s) stored in the frontend class) and then
		// hook the presentation to the logic.
		GET_PRESENTATION: {
			for (Theme t : frontend.getThemes()) {
				presentation = t.supply(this);
				if (presentation != null)
					try {
						presentation.show();
						break GET_PRESENTATION;
					} catch (WindowLoadFailureException e) {
						ArlithFrontend.getGuiLogger().wrn(
								"Failed to present a GUI to the user with a specific theme. Printing error and trying next theme, if there are any.");
						ArlithFrontend.getGuiLogger().err(e);
					}
			}
			// We get here if no presentation can be shown
			throw new SceneShowFailureException(
					"Couldn't present the user interface to the user. (No active theme supports the UI.)", this);
		}
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
