package pala.apps.arlith.frontend.clientgui.themes.gray;

import pala.apps.arlith.frontend.clientgui.Logic;
import pala.apps.arlith.frontend.clientgui.Presentation;
import pala.apps.arlith.frontend.clientgui.Theme;
import pala.apps.arlith.frontend.clientgui.themes.gray.login.LogInPresentationImpl;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;

public class GrayTheme implements Theme {

	@SuppressWarnings("unchecked")
	@Override
	public <P extends Presentation<L>, L extends Logic<P>> P supply(L userInterface) {
		if (userInterface instanceof LogInLogic)
			return (P) new LogInPresentationImpl((LogInLogic) userInterface);
		return null;
	}

}
