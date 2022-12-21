package pala.apps.arlith.graphics;

import static javafx.scene.paint.Color.GOLD;
import static javafx.scene.paint.Color.RED;
import static pala.apps.arlith.frontend.guis.GUIUtils.applyClickAnimation;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import pala.apps.arlith.application.Logging;
import pala.apps.arlith.backend.client.api.ClientOwnUser;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.graphics.nodes.MemberCard;
import pala.apps.arlith.frontend.guis.communitylistview.CommunityListViewPage;
import pala.apps.arlith.frontend.guis.home.HomePage;
import pala.apps.arlith.frontend.guis.settings.SettingsPage;
import pala.apps.arlith.frontend.guis.threadlistview.ThreadListViewPage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

public @Disposable class LeftPanel {

	private final ArlithWindow win;

	public LeftPanel(ArlithWindow win) {
		this.win = win;
	}

	public void dispose() {
		mc.dispose();
	}

	public @FXML Node threadsButton;
	public @FXML Shape threadsTitle;
	public @FXML Shape threadsButtonI1;
	public @FXML Node communitiesButton;
	public @FXML Shape communitiesTitle;
//	public @FXML Node friendsButton;
//	public @FXML Shape friendsTitle;
//	public @FXML Node appearanceButton;
//	public @FXML Shape appearanceTitle;
//	public @FXML Node integrationsButton;
//	public @FXML Shape integrationsTitle;
	public @FXML Node homeButton;
	public @FXML Shape homeTitle;
	public @FXML Node settingsButton;
	public @FXML Shape settingsTitle;
	private @FXML VBox leftPanel;
	private MemberCard mc;

	private @FXML void initialize() {
		applyClickAnimation(threadsButton, GOLD, RED, threadsTitle, threadsButtonI1);
		applyClickAnimation(communitiesButton, GOLD, RED, communitiesTitle);
//		applyClickAnimation(friendsButton, GOLD, RED, friendsTitle);
//		applyClickAnimation(appearanceButton, GOLD, RED, appearanceTitle);
//		applyClickAnimation(integrationsButton, GOLD, RED, integrationsTitle);
		applyClickAnimation(homeButton, GOLD, RED, homeTitle);
		applyClickAnimation(settingsButton, GOLD, RED, settingsTitle);

		try {
			ClientOwnUser ou = win.getApplication().getClient().getOwnUser();
			mc = new MemberCard(ou);
			leftPanel.getChildren().add(0, mc);
			VBox.setMargin(mc, new Insets(5, 0, 0, 0));
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to retrieve information about your own user from the server.");
			Logging.err(e);
		}

		homeButton.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				try {
					win.show(new HomePage());
				} catch (WindowLoadFailureException e) {
					Logging.err("Failed to load the home window.");
					Logging.err(e);
				}
		});
		settingsButton.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				try {
					win.show(new SettingsPage());
				} catch (WindowLoadFailureException e) {
					Logging.err("Failed to load the home window.");
					Logging.err(e);
				}
		});
		threadsButton.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				try {
					win.show(new ThreadListViewPage());
				} catch (WindowLoadFailureException e) {
					Logging.err("Failed to load the home window.");
					Logging.err(e);
				}
		});
		communitiesButton.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				try {
					win.show(new CommunityListViewPage());
				} catch (WindowLoadFailureException e) {
					Logging.err("Failed to load the community window.");
					Logging.err(e);
				}
		});
	}
}
