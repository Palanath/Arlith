package pala.apps.arlith.frontend.clientgui.themes.gray.home;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.clientgui.uispec.home.HomeLogic;
import pala.apps.arlith.frontend.clientgui.uispec.home.HomePresentation;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public class HomePresentationImpl implements HomePresentation {

	/**
	 * List containing rectangular {@link UserListItem}s on the left-hand side of
	 * the UI.
	 */
	private @FXML VBox userList;
	/**
	 * {@link VBox} where messages are shown. This {@link VBox} physically appears
	 * on the right hand side of the home UI. It contains {@link HBox}es whenver
	 * there are messages being shown in it. Each {@link HBox} represents a
	 * <i>block</i> of messages from a single user. A block of messages will have
	 * one or more messages in it.
	 */
	private @FXML VBox messagePane;

	public HomePresentationImpl(HomeLogic logic) {
		this.logic = logic;
	}

	private final HomeLogic logic;

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
