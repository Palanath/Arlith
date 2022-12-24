package pala.apps.arlith.frontend.guis.communitylistview;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.frontend.guis.GUIUtils;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.apps.arlith.libraries.graphics.windows.Page;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class NewCommunityViewPage implements Page {

	private @FXML Rectangle backArrow1, backArrow2, backArrow3;

	private final Transition backgroundTransition = new Transition() {

		{
			setCycleDuration(Duration.millis(800));
		}

		@Override
		protected void interpolate(double frac) {
			serverBackground.setOpacity(frac * .3);
		}
	};

	private @FXML TextField serverNamePrompt;
	private @FXML ImageView serverIcon;
	private final ImageView serverBackground = new ImageView();
	{
		serverBackground.setOpacity(0);
	}
	private @FXML HBox iconEditBox, backgroundEditBox;
	private @FXML Button changeIconButton, removeIconButton;
	private final Button setIconButton = new Button("Set"), setBackgroundButton = new Button("Set");
	{
		setIconButton.getStyleClass().add("pop-button");
		setBackgroundButton.getStyleClass().add("pop-button");
	}
	private byte[] icon, bg;
	private final FileChooser fc = new FileChooser();
	private ArlithWindow window;

	private @FXML void changeIcon() {
		File f = fc.showOpenDialog(window.getApplication().getStage());
		if (f != null) {
			fc.setInitialDirectory(f.getParentFile());
			byte[] img;
			try {
				img = Files.readAllBytes(f.toPath());
			} catch (IOException e) {
				GUIUtils.getGuiLogger().err("An IO error occurred while reading the file.");
				return;
			} catch (OutOfMemoryError e) {
				GUIUtils.getGuiLogger()
						.err("Could not load the specified image as an icon. The file is too large to fit in RAM.");
				return;
			}
			Image image = new Image(new ByteArrayInputStream(img));
			if (image.isError())
				GUIUtils.getGuiLogger().err("Couldn't load an image from the specified file.");
			else {
				icon = img;
				serverIcon.setImage(image);
			}
		}
	}

	private @FXML void removeIcon() {
		icon = null;
		serverIcon.setImage(null);
		iconEditBox.getChildren().setAll(setIconButton);
	}

	private @FXML StackPane backgroundBox;
	private @FXML Text backgroundStatusText;
	private @FXML Button changeBackgroundButton, removeBackgroundButton;

	private @FXML void changeBackground() {
		File f = fc.showOpenDialog(window.getApplication().getStage());
		if (f != null) {
			fc.setInitialDirectory(f.getParentFile());
			byte[] img;
			try {
				img = Files.readAllBytes(f.toPath());
			} catch (IOException e) {
				GUIUtils.getGuiLogger().err("An IO error occurred while reading the file.");
				return;
			} catch (OutOfMemoryError e) {
				GUIUtils.getGuiLogger()
						.err("Could not load the specified image as an icon. The file is too large to fit in RAM.");
				return;
			}
			Image image = new Image(new ByteArrayInputStream(img));
			if (image.isError())
				GUIUtils.getGuiLogger().err("Couldn't load an image from the specified file.");
			else {
				bg = img;
				serverBackground.setImage(image);
			}
		}
	}

	private @FXML void removeBackground() {
		bg = null;
		serverBackground.setImage(null);
		backgroundStatusText.setText("EMPTY");
		backgroundStatusText.setStyle("-fx-fill: firebrick;");
		backgroundEditBox.getChildren().setAll(setBackgroundButton);
	}

	@Override
	public void cleanup(ArlithWindow window) {
		window.getRoot().getChildren().remove(serverBackground);
	}

	@Override
	public void show(ArlithWindow window) throws WindowLoadFailureException {
		this.window = window;
		FXMLLoader loader = new FXMLLoader(NewCommunityViewPage.class.getResource("NewCommunityView.fxml"));
		loader.setController(this);
		try {
			window.setContent(loader.load());
		} catch (IOException e) {
			throw new WindowLoadFailureException(e);
		}

		HashSet<Node> nodes = new HashSet<>();
		nodes.add(backArrow1);
		nodes.add(backArrow2);
		nodes.add(backArrow3);
		GUIUtils.applyClickAnimation(nodes, Color.GOLD, Color.RED, backArrow1, backArrow2, backArrow3);
		backArrow1.setCursor(Cursor.HAND);
		backArrow2.setCursor(Cursor.HAND);
		backArrow3.setCursor(Cursor.HAND);
		EventHandler<? super MouseEvent> backHandler = event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				goBack();
		};
		backArrow1.setOnMouseClicked(backHandler);
		backArrow2.setOnMouseClicked(backHandler);
		backArrow3.setOnMouseClicked(backHandler);

		// TODO Finish
		window.getRoot().getChildren().add(0, serverBackground);
		FXTools.setAllAnchors(0, serverBackground);
		serverBackground.fitWidthProperty().bind(window.getRoot().widthProperty());
		serverBackground.fitHeightProperty().bind(window.getRoot().heightProperty());

		iconEditBox.getChildren().setAll(setIconButton);
		backgroundEditBox.getChildren().setAll(setBackgroundButton);
		setIconButton.setOnAction(event -> {
			File f = fc.showOpenDialog(window.getApplication().getStage());
			if (f != null) {
				fc.setInitialDirectory(f.getParentFile());
				byte[] img;
				try {
					img = Files.readAllBytes(f.toPath());
				} catch (IOException e1) {
					GUIUtils.getGuiLogger().err("An IO error occurred while reading the file.");
					return;
				} catch (OutOfMemoryError e2) {
					GUIUtils.getGuiLogger()
							.err("Could not load the specified image as an icon. The file is too large to fit in RAM.");
					return;
				}
				Image image = new Image(new ByteArrayInputStream(img));
				if (image.isError())
					GUIUtils.getGuiLogger().err("Couldn't load an image from the specified file.");
				else {
					icon = img;
					serverIcon.setImage(image);
					iconEditBox.getChildren().setAll(changeIconButton, removeIconButton);
				}
			}
		});

		setBackgroundButton.setOnAction(event -> {
			File f = fc.showOpenDialog(window.getApplication().getStage());
			if (f != null) {
				fc.setInitialDirectory(f.getParentFile());
				byte[] img;
				try {
					img = Files.readAllBytes(f.toPath());
				} catch (IOException e1) {
					GUIUtils.getGuiLogger().err("An IO error occurred while reading the file.");
					return;
				} catch (OutOfMemoryError e2) {
					GUIUtils.getGuiLogger()
							.err("Could not load the specified image as an icon. The file is too large to fit in RAM.");
					return;
				}
				Image image = new Image(new ByteArrayInputStream(img));
				if (image.isError())
					GUIUtils.getGuiLogger().err("Couldn't load an image from the specified file.");
				else {
					bg = img;
					serverBackground.setImage(image);
					backgroundStatusText.setText("HOVER OVER ME");
					backgroundStatusText.setStyle("-fx-fill: -stuff-text-fill;");
					backgroundEditBox.getChildren().setAll(changeBackgroundButton, removeBackgroundButton);
				}
			}
		});

		backgroundBox.hoverProperty().addListener((observable, oldValue, newValue) -> {
			Duration dur = backgroundTransition.getCurrentTime();
			backgroundTransition.stop();
			backgroundTransition.setRate(newValue ? 1 : -1);
			backgroundTransition.playFrom(dur);
//					.playFrom(backgroundTransition.getCycleDuration().subtract(backgroundTransition.getCurrentTime()));
		});

	}

	private @FXML void complete() {
		ArlithClient cli = window.getApplication().getClient();
		try {
			cli.createCommunity(serverNamePrompt.getText(), icon, bg);
		} catch (CommunicationProtocolError | RuntimeException e) {
			GUIUtils.getGuiLogger().err(e);
			return;
		}

		FXTools.spawnLabelAtMousePos("Success", Color.GREEN, window.getApplication().getStage());

		goBack();
	}

	private void goBack() {
		try {
			new CommunityListViewPage().show(window);
		} catch (WindowLoadFailureException e) {
			GUIUtils.getGuiLogger().err("Failed to show the community list page.");
			GUIUtils.getGuiLogger().err(e);
		}
	}

}
