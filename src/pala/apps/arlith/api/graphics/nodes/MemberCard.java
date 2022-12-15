package pala.apps.arlith.api.graphics.nodes;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.app.client.api.ClientUser;
import pala.apps.arlith.app.guis.GUIUtils;
import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.graphics.Disposable;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.javafx.bindings.BindingTools;

public @Disposable class MemberCard extends StackPane {
	private static final Duration CLICK_AND_RELEASE_ANIMATION_TIME = Duration.seconds(0.2);
	private final ClientUser user;
	private final ImageView icon = new ImageView();
	private final Text username = new Text(), gid = new Text(), info = new Text("Click to copy member card");
	private final VBox box = new VBox(icon, username, gid, info);
	private final Shape statusIcon = new Circle(9.5);

	private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GOLD);

	private Color from = Color.GOLD, to = Color.RED;

	public void dispose() {

	}

	{
		box.setPadding(new Insets(10, 20, 10, 20));
		// TODO Verify that these following size constraints are necessary.

		box.setAlignment(Pos.CENTER);
		box.setStyle("-fx-background-color:derive(-stuff-dark,5%);-fx-background-radius:20px;");

		icon.setFitHeight(96);
		icon.setPreserveRatio(true);

		username.fillProperty().bind(color);
		gid.fillProperty().bind(BindingTools.mask(color, a -> a.deriveColor(0, 1, 1, 0.6)));
		info.fillProperty().bind(BindingTools.mask(color, a -> a.deriveColor(0, 1, 1, 0.3)));
		username.setFont(Font.font(24));
		gid.setFont(Font.font(18));
		info.setFont(Font.font(13));

		setAlignment(statusIcon, Pos.TOP_RIGHT);
		setMargin(statusIcon, new Insets(20, 20, 0, 0));

		GUIUtils.applyClickAnimation(this, from, to, color::set);

		getChildren().setAll(box, statusIcon);
	}

	public MemberCard(ClientUser user) {
		this.user = user;
		user.profileIconView().register((previousValue, newValue) -> icon.setImage(newValue));
		setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				ClipboardContent content = new ClipboardContent();
				try {
					if (event.isControlDown()) {
						content.putString("Username: " + user.getIdentifier() + "\nGID: " + user.id());
						content.putHtml("<username>" + user.getIdentifier() + "</><gid>" + user.id() + "</>");
						if (Clipboard.getSystemClipboard().setContent(content))
							FXTools.spawnLabelAtMousePos("Copied everything", Color.GREEN,
									getScene().getWindow());
						else
							FXTools.spawnLabelAtMousePos("Failed to copy...", Color.FIREBRICK,
									getScene().getWindow());
					} else if (event.isAltDown())
						if (icon.getImage() != null) {
							content.putImage(icon.getImage());
							if (Clipboard.getSystemClipboard().setContent(content))
								FXTools.spawnLabelAtMousePos("Copied PFP", Color.GREEN, getScene().getWindow());
							else
								FXTools.spawnLabelAtMousePos("Failed to copy...", Color.FIREBRICK,
										getScene().getWindow());
						} else
							FXTools.spawnLabelAtMousePos("No icon to copy", Color.GOLD, getScene().getWindow());
					else {
						content.putString(user.id().toString());
						if (Clipboard.getSystemClipboard().setContent(content))
							FXTools.spawnLabelAtMousePos("Copied user ID", Color.GREEN, getScene().getWindow());
						else
							FXTools.spawnLabelAtMousePos("Failed to copy...", Color.FIREBRICK,
									getScene().getWindow());
					}
				} catch (CommunicationProtocolError | RuntimeException e) {
					Logging.err("Failed to obtain client's username for storing in clipboard.");
				}
			}
		});
		statusIcon.setFill(Color.RED);

		try {
			icon.setImage(user.getProfileIcon());
		} catch (CommunicationProtocolError | RuntimeException e1) {
			Logging.err("Failed to get the profile icon of a user to display in a member card.");
			Logging.err(e1);
		}

		try {
			user.usernameView().register((previousValue, name) -> {
				if (name.length() > 16)
					name = name.substring(0, 14) + "...";
				username.setText(name);
			});
			String name = user.getIdentifier();
			if (name.length() > 16)
				name = name.substring(0, 14) + "...";
			username.setText(name);
			String id = user.idHex();
			if (id.length() > 15)
				id = id.substring(0, 16) + "...";
			gid.setText(id);
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to retrieve user information.");
			Logging.err(e);
		}
	}

}
