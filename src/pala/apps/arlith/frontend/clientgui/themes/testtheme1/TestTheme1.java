package pala.apps.arlith.frontend.clientgui.themes.testtheme1;

import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Presentation;
import pala.apps.arlith.frontend.clientgui.Theme;
import pala.apps.arlith.frontend.clientgui.themes.testtheme1.login.LogInPresentationImpl;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;

public class TestTheme1 implements Theme {

	@SuppressWarnings("unchecked")
	@Override
	public <P extends Presentation<L>, L extends Logic<P>> P supply(L userInterface) {
		if (userInterface instanceof LogInLogic)
			return (P) new LogInPresentationImpl((LogInLogic) userInterface);
		return null;
	}

}
