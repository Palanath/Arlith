package pala.apps.arlith.frontend.guis.communitylistview;

import java.io.IOException;

import javafx.animation.Transition;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.apps.arlith.backend.client.api.ClientCommunity;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.guis.BindHandlerPage;
import pala.apps.arlith.frontend.guis.BindHandlerPage.PageBindable;

public class CommunityList {
	private @FXML VBox root;
	private @FXML FlowPane contentBox;
	private ImageView backgroundIV;
	private final BindHandlerPage page;

	private final Transition backgroundTransition = new Transition() {

		{
			setCycleDuration(Duration.millis(800));
		}

		@Override
		protected void interpolate(double frac) {
			backgroundIV.setOpacity(frac * .08);
		}
	};

	{
		FXMLLoader loader = new FXMLLoader(CommunityList.class.getResource("CommunityList.fxml"));
		loader.setController(this);
		loader.load();
	}

	public void setBackgroundIV(ImageView backgroundIV) {
		this.backgroundIV = backgroundIV;
	}

	public ImageView getBackgroundIV() {
		return backgroundIV;
	}

	public CommunityList(BindHandlerPage page) throws IOException {
		this.page = page;
	}

	public VBox getRoot() {
		return root;
	}

	public FlowPane getContentBox() {
		return contentBox;
	}

	public class Listing {

		private final PageBindable<Image> iconBinding;

		public void remove() {
			CommunityList.this.contentBox.getChildren().remove(root);
			iconBinding.unbind();
		}

		public void add(int pos) {
			CommunityList.this.contentBox.getChildren().add(pos, root);
			iconBinding.bind(community.iconView());
		}

		public void add() {
			CommunityList.this.contentBox.getChildren().add(root);
		}

		private @FXML StackPane root;
		private @FXML ImageView icon;
		private @FXML Text name;
		private final Rectangle cropper = new Rectangle(300, 300);
		private final ClientCommunity community;

		{
			cropper.setArcWidth(20);
			cropper.setArcHeight(20);
			FXMLLoader loader = new FXMLLoader(CommunityList.class.getResource("CommunityListing.fxml"));
			loader.setController(this);
			loader.load();
			icon.setClip(cropper);
		}

		/**
		 * Creates a {@link CommunityList} listing based off of the provided community.
		 * 
		 * @param community The community to list.
		 * @throws IOException If loading the FXML Nodes for this listing, from their
		 *                     FXML file, fails.
		 */
		public Listing(ClientCommunity community) throws IOException {
			this.community = community;

			try {
				icon.setImage(community.getIcon());
				Image img = icon.getImage();
				double w = img.getWidth(), h = img.getHeight();
				icon.setViewport(new Rectangle2D(.1 * w, .1 * h, w * .8, h * .8));
			} catch (CommunicationProtocolError | RuntimeException e) {
				ArlithFrontend.getGuiLogger().err("Failed to load a community's icon. (Community name: " + community.getName()
						+ ", id: " + community.idHex() + ')');
			}
			(iconBinding = page.bindable(t -> {
				icon.setImage(t);
				double w = t.getWidth(), h = t.getHeight();
				icon.setViewport(new Rectangle2D(.1 * w, .1 * h, w * .8, h * .8));
			})).bind(community.iconView());

			try {
				community.getBackgroundImage();// Load the image.
			} catch (CommunicationProtocolError | RuntimeException e) {
				ArlithFrontend.getGuiLogger().err("Failed to load a community's background image. (Community name: "
						+ community.getName() + ", id: " + community.idHex() + ')');
			}

			name.setText(community.getName());
			root.setBackground(CommunityListViewPage.NEW_COMMUNITY_DEFAULT_BACKGROUND);

			root.hoverProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue)
					try {
						backgroundIV.setImage(community.getBackgroundImage());
					} catch (CommunicationProtocolError | RuntimeException e) {
						// Handle differently as this can only occur if it fails the first time.
						ArlithFrontend.getGuiLogger().err("Failed to load a community's background image. (Community name: "
								+ community.getName() + ", id: " + community.idHex() + ')');
					}
				Duration dur = backgroundTransition.getCurrentTime();
				backgroundTransition.stop();
				backgroundTransition.setRate(newValue ? 1 : -1);
				backgroundTransition.playFrom(dur);
			});
			root.setOnMouseMoved(event -> {
				Image img = icon.getImage();
				if (img == null)
					return;
				double w = img.getWidth(), h = img.getHeight();
				icon.setViewport(new Rectangle2D(.2 * w * event.getX() / root.getWidth(),
						.2 * h * event.getY() / root.getHeight(), w * .8, h * .8));
			});
			root.setOnMouseExited(e -> {
				Image img = icon.getImage();
				if (img == null)
					return;
				double w = img.getWidth(), h = img.getHeight();
				icon.setViewport(new Rectangle2D(.1 * w, .1 * h, w * .8, h * .8));
			});
			root.setOnMouseClicked(new EventHandler<Event>() {

				@Override
				public void handle(Event event) {
					// TODO Auto-generated method stub

				}
			});
			root.setCursor(Cursor.HAND);

			add();

		}

	}

}
