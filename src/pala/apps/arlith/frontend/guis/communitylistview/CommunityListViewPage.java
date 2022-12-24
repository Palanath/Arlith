package pala.apps.arlith.frontend.guis.communitylistview;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.ClientCommunity;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.frontend.guis.BindHandlerPage;
import pala.apps.arlith.frontend.guis.GUIUtils;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.JavaTools;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class CommunityListViewPage extends BindHandlerPage {

	static final double BACKGROUND_IMAGE_OPACITY_LEVEL = 0.3;

	static final Background NEW_COMMUNITY_DEFAULT_BACKGROUND = FXTools
			.getBackgroundFromColor(new Color(.14, .14, .14, .58), 10),
			NEW_COMMUNITY_HOVER_BACKGROUND = FXTools.getBackgroundFromColor(new Color(.04, .04, .04, .38), 10);
	private CommunityList myCommunities, browseCommunities;
	private @FXML Tab myCommunitiesTab, browseCommunitiesTab;
	private ImageView iv;
	private ArlithClient client;

	@Override
	public void cleanup(ArlithWindow window) {
		window.getRoot().getChildren().remove(iv);
//		client.unregister(CommunityCreatedEvent.COMMUNITY_CREATED_EVENT, newCommunityHandler);
	}

	@Override
	public void show(ArlithWindow window) throws WindowLoadFailureException {
		client = window.getApplication().getClient();
		try {
			myCommunities = new CommunityList(this);
			browseCommunities = new CommunityList(this);
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}

		window.getMenuBar().title.setText("COMMUNITIES");
		iv = new ImageView();
		iv.setOpacity(0);
		iv.fitWidthProperty().bind(window.getRoot().widthProperty());
		iv.fitHeightProperty().bind(window.getRoot().heightProperty());
		FXTools.setAllAnchors(0, iv);
		window.getRoot().getChildren().add(0, iv);
		myCommunities.setBackgroundIV(iv);
		browseCommunities.setBackgroundIV(iv);

		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		try {
			window.setContent(
					loader.load(CommunityListViewPage.class.getResourceAsStream("CommunityListViewGUI.fxml")));
		} catch (IOException e) {
			throw new WindowLoadFailureException("Failed to load the Community page.", e);
		}

		StackPane newButtonRoot;
		try {
			myCommunities.getContentBox().getChildren().add(
					newButtonRoot = new FXMLLoader(CommunityListViewPage.class.getResource("NewCommunityListing.fxml"))
							.load());
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}
		myCommunitiesTab.setContent(myCommunities.getRoot());
		browseCommunitiesTab.setContent(browseCommunities.getRoot());
		newButtonRoot.setBackground(NEW_COMMUNITY_DEFAULT_BACKGROUND);

		try {
			List<ClientCommunity> communities = JavaTools.paginate(1, 10, client.listJoinedCommunities());
			for (ClientCommunity com : communities) {
				try {
					myCommunities.new Listing(com);
				} catch (IOException e) {
					GUIUtils.getGuiLogger().err("Displaying a community in the community list failed.");
					GUIUtils.getGuiLogger().err(e);
				}
			}
		} catch (CommunicationProtocolError | RuntimeException e) {
			GUIUtils.getGuiLogger()
					.err("Failed to load the communities you're in (as well as the ones publicly available).");
			GUIUtils.getGuiLogger().err(e);
		}

//		client.register(CommunityCreatedEvent.COMMUNITY_CREATED_EVENT, newCommunityHandler);

		newButtonRoot.hoverProperty().addListener((observable, oldValue, newValue) -> newButtonRoot
				.setBackground(newValue ? NEW_COMMUNITY_HOVER_BACKGROUND : NEW_COMMUNITY_DEFAULT_BACKGROUND));
		newButtonRoot.setCursor(Cursor.HAND);
		newButtonRoot.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				try {
					new NewCommunityViewPage().show(window);
				} catch (WindowLoadFailureException e) {
					GUIUtils.getGuiLogger().err("Failed to show the create community window.");
					GUIUtils.getGuiLogger().err(e);
				}
		});

	}

}
