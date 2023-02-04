package pala.apps.arlith.frontend.clientgui.logic.home;

import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend;
import pala.apps.arlith.frontend.clientgui.ClientGUIFrontend.UserInterface;
import pala.apps.arlith.frontend.clientgui.uispec.home.HomeLogic;
import pala.apps.arlith.frontend.clientgui.uispec.home.HomePresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class HomeScene extends UserInterface implements HomeLogic {

	private HomePresentation presentation;

	public HomeScene(ClientGUIFrontend frontend) {
		frontend.super();
	}

	@Override
	protected void show(Stage stage) throws WindowLoadFailureException {
		presentation = loadPresentation();
		presentation.show(stage);
	}

}
