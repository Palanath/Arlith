package pala.apps.arlith.app.guis.home;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pala.apps.arlith.app.guis.ApplicationState;
import pala.apps.arlith.app.guis.BindHandlerPage;
import pala.apps.arlith.app.guis.home.center.FriendsTab;
import pala.apps.arlith.app.guis.home.center.HomeTab;
import pala.apps.arlith.app.guis.home.center.PanelTab;
import pala.apps.arlith.app.guis.home.center.StatsTab;
import pala.apps.arlith.application.Logging;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class HomePage extends BindHandlerPage {

	public VBox root;
	public @FXML TabPane content;
	public PanelTab[] tabs;

	public void destroy() {
		for (PanelTab pt : tabs)
			pt.destroy();
	}

	private @FXML Text welcomeUsername;

	private ArlithWindow window;

	public ApplicationState getApp() {
		return window.getApplication();
	}

	public ArlithWindow getWindow() {
		return window;
	}

	@Override
	public void cleanup(ArlithWindow window) {
		super.cleanup(window);
		destroy();
	}

	@Override
	public void show(ArlithWindow win) throws WindowLoadFailureException {
		win.getMenuBar().title.setText("HOME");
		window = win;
		Stage stage = (Stage) win.getContentRoot().getScene().getWindow();
		stage.setMaxHeight(Double.MAX_VALUE);
		stage.setMaxWidth(Double.MAX_VALUE);

		stage.setMinWidth(900);
		stage.setMinHeight(600);
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Home Center Panel.fxml"));
			loader.setController(this);
			root = loader.load();
			tabs = new PanelTab[] { new HomeTab(this), new FriendsTab(this), new StatsTab(this) };
			win.setContent(root);
		} catch (IOException e) {
			Logging.err("Failed to load the CENTER PANEL of the home window.");
			throw new WindowLoadFailureException(e);
		}

		content.getTabs().addAll(tabs);
	}

}
