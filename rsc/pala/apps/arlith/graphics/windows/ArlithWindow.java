package pala.apps.arlith.graphics.windows;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.guis.ApplicationState;
import pala.apps.arlith.graphics.Disposable;
import pala.apps.arlith.graphics.LeftPanel;
import pala.apps.arlith.graphics.MenuBar;
import pala.apps.arlith.libraries.graphics.windows.Page;
import pala.libs.generic.guis.ApplicationProperties;
import pala.libs.generic.guis.Window;
import pala.libs.generic.javafx.FXTools;

/**
 * Represents a graphical user interface window in Arlith. This class provides a
 * few facilities and offers a small framework for Arlith GUI windows.
 * 
 * @author Palanath
 *
 */
public @Disposable class ArlithWindow extends Window {
	private final LeftPanel leftPanel;
	private final AnchorPane root = new AnchorPane();
	private final BorderPane contentRoot = new BorderPane();
	{
		root.getChildren().add(contentRoot);
		FXTools.setAllAnchors(0, contentRoot);
	}
	private final MenuBar menuBar = new MenuBar();
	private final ApplicationState appState;
	private Page page;

	public void setContent(Node node) {
		contentRoot.setCenter(node);
	}

	public LeftPanel getLeftPanel() {
		return leftPanel;
	}

	public synchronized ArlithWindow show(Page page) throws WindowLoadFailureException {
		if (displayed()) {
			if (this.page != null)
				this.page.cleanup(this);
			try {
				if (page != null)
					page.show(this);
			} catch (WindowLoadFailureException e) {
				try {
					page.cleanup(this);
				} catch (Exception e1) {
					e.addSuppressed(e1);
				}
				throw e;
			}
		}
		this.page = page;
		return this;
	}

	public ApplicationState getApplication() {
		return appState;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public BorderPane getContentRoot() {
		return contentRoot;
	}

	public AnchorPane getRoot() {
		return root;
	}

	@Override
	public void destroy() {
		leftPanel.dispose();
	}

	public ArlithWindow(ApplicationState appState) {
		this.appState = appState;
		leftPanel = new LeftPanel(this);
	}

	@Override
	protected synchronized void show(Stage stage, ApplicationProperties properties) throws WindowLoadFailureException {
		root.getStylesheets().setAll(properties.themeStylesheet.get());

		FXMLLoader loader = new FXMLLoader();
		loader.setController(leftPanel);
		try {
			contentRoot.setLeft(loader.load(LeftPanel.class.getResourceAsStream("Left Panel.fxml")));
		} catch (IOException e) {
			ArlithFrontend.getGuiLogger().err("Failed to load the main application layout.");
			ArlithFrontend.getGuiLogger().err(e);
			throw new WindowLoadFailureException(e);
		}

		loader = new FXMLLoader();
		loader.setController(menuBar);
		try {
			contentRoot.setTop(loader.load(MenuBar.class.getResourceAsStream("Menu Bar.fxml")));
		} catch (IOException e) {
			ArlithFrontend.getGuiLogger().err("Failed to load the main application layout.");
			ArlithFrontend.getGuiLogger().err(e);
			throw new WindowLoadFailureException(e);
		}
		stage.setScene(stage.getScene() == null ? new Scene(root)
				: new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
		if (page != null)
			page.show(this);
	}

}
