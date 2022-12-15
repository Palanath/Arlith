package pala.apps.arlith.app.guis.home.center;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import pala.apps.arlith.app.guis.home.HomePage;

public class PanelTab extends Tab {
	protected Node root;
	protected final HomePage home;

	public PanelTab(String resource, HomePage home) throws IOException {
		this(resource, home, null);
	}

	public PanelTab(String resource, HomePage home, String name) throws IOException {
		this.home = home;
		FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
		loader.setController(this);
		setContent(root = loader.load());
		setText(name);
	}

	public void destroy() {
	}

	public PanelTab(HomePage home) {
		this.home = home;
	}

}
