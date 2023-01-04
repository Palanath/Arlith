package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.scene.Scene;
import pala.apps.arlith.backend.common.protocol.types.LoginProblemValue;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class LogInPresentationImpl implements LogInPresentation {
	private final LogInLogic logic;

	public LogInPresentationImpl(LogInLogic logic) {
		this.logic = logic;
	}

	@Override
	public Scene getScene() throws WindowLoadFailureException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showLoginProblem(LoginProblemValue problem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lockUIForLoggingIn() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unlockUIForLoggingIn() {
		// TODO Auto-generated method stub

	}

}
