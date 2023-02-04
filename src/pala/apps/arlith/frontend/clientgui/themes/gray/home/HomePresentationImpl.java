package pala.apps.arlith.frontend.clientgui.themes.gray.home;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.uispec.home.HomePresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class HomePresentationImpl implements HomePresentation {

	private @FXML void initialize() {

	}

	@Override
	public void show(Stage stage) throws WindowLoadFailureException {
		FXMLLoader loader = new FXMLLoader(HomePresentationImpl.class.getResource("HomeGUI.fxml"));
		loader.setController(this);
		try {
			stage.setScene(new Scene(loader.load()));
		} catch (IOException e) {
			throw new WindowLoadFailureException("Failed to load the Home GUI.", e);
		}
	}

}
