package pala.apps.arlith.app.guis.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pala.apps.arlith.app.client.api.ClientOwnUser;
import pala.apps.arlith.app.guis.ApplicationState;
import pala.apps.arlith.app.guis.BindHandlerPage;
import pala.apps.arlith.app.guis.GUIUtils;
import pala.apps.arlith.app.logging.Logging;
import pala.apps.arlith.backend.Utilities;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.JavaTools;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

public class SettingsPage extends BindHandlerPage {

	private @FXML Text title, profileTitle;
	private @FXML HBox profileMenu;
	private @FXML VBox menuList;
	private @FXML ImageView pfiview;
	private @FXML TextField usernamePrompt, emailPrompt, phonePrompt, currentPasswordPrompt, newPasswordPrompt;
	private @FXML Rectangle rollpivot, shadowbox;
	private @FXML Button changePFIButton, removePFIButton;
	private ApplicationState app;

	private final ObjectProperty<HBox> selectedMenu = new SimpleObjectProperty<>();
	{
		selectedMenu.addListener((observable, oldValue, newValue) -> {
			if (oldValue != null)
				oldValue.setStyle("");
			if (newValue != null)
				newValue.setStyle("-fx-background-radius:5px;-fx-background-color:derive(-stuff-light,-10%);");
		});
	}

	@Override
	public void show(ArlithWindow win) throws WindowLoadFailureException {
		app = win.getApplication();
		win.getMenuBar().title.setText("SETTINGS");
		Stage stage = (Stage) win.getContentRoot().getScene().getWindow();
		stage.setMaxHeight(Double.MAX_VALUE);
		stage.setMaxWidth(Double.MAX_VALUE);

		stage.setMinHeight(400);
		stage.setMinWidth(600);

		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		try {
			win.setContent(loader.load(SettingsPage.class.getResourceAsStream("SettingsGUI.fxml")));
		} catch (IOException e) {
			Logging.err("Failed to load the settings menu.");
			Logging.err(e);
			throw new WindowLoadFailureException(e);
		}

		ChangeListener<? super String> listener = (observable, oldValue, newValue) -> {
			usernamePrompt.setBorder(null);
			emailPrompt.setBorder(null);
			phonePrompt.setBorder(null);
		};
		usernamePrompt.textProperty().addListener(listener);
		emailPrompt.textProperty().addListener(listener);
		phonePrompt.textProperty().addListener(listener);

		PROMPT_FILL_BLOCK: {
			ClientOwnUser ownusr;
			try {
				ownusr = win.getApplication().getClient().getOwnUser();
			} catch (RuntimeException | CommunicationProtocolError e) {
				Logging.err("Failed to obtain username.");
				Logging.err(e);
				break PROMPT_FILL_BLOCK;
			}
			usernamePrompt.setText(ownusr.name());
			try {
				emailPrompt.setText(ownusr.getEmail());
			} catch (CommunicationProtocolError | RuntimeException e) {
				Logging.err("Failed to contact the server while loading the settings menu.");
				Logging.err(e);
			}
			try {
				phonePrompt.setText(ownusr.getPhoneNumber());
			} catch (CommunicationProtocolError | RuntimeException e) {
				Logging.err("Failed to contact the server while loading the settings menu.");
				Logging.err(e);
			}
		}

		try {
			pfiview.setImage(app.getClient().getOwnUser().getProfileIcon());
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to retrieve PFI.");
			Logging.err(e);
		}
		try {
			bindable(pfiview::setImage).bind(app.getClient().getOwnUser().profileIconView());
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err(e);
		}

		int maxroll = 40;// Maximum degrees that the image will rotate along its axis (when mouse is on
							// edge of image, i.e. it's farthest from the center).

		// Render a black and transparent linear gradient on top of the image.
		ColorInput ci = new ColorInput();
		ci.setWidth(192);// Image width
		ci.setHeight(192);// Img height
		ci.setPaint(Color.TRANSPARENT);

		// Render the image as if it were under a light source.
		Light.Distant light = new Light.Distant();
		Lighting lighting = new Lighting(light);
		lighting.setSurfaceScale(3);// Make the image pOp!

		// Combine the black "shadow" gradient atop the lit image.
		Blend blend = new Blend(BlendMode.SRC_ATOP);
		blend.setTopInput(ci);
		blend.setBottomInput(lighting);

		pfiview.setEffect(blend);// Apply the effect.

		// Assign code to execute whenever the mouse moves over the image.
		rollpivot.setOnMouseMoved(event -> {

			///// Code to execute when mouse moves. /////

			// Get position of mouse relative to center of node.
			double mx = event.getX() / (rollpivot.getWidth() / 2) - 1,
					my = event.getY() / (rollpivot.getHeight() / 2) - 1;

			// Get the magnitude of the vector representing the mouse's position from the
			// center (basically, get the distance of the mouse from the center of the
			// image).
			double mag = Math.sqrt(mx * mx + my * my);// Pythagorean Theorem :D

			// Assign the rotation axis on which the image will rotate.
			// We change the axis based off of where the mouse is because we need the corner
			// of the image closest to the mouse to tilt into the screen.
			pfiview.setRotationAxis(new Point3D(my, -mx, 0));
			// Set the *amount* to rotate the image on its axis ^^^
			pfiview.setRotate(maxroll * mag);

			// Set which direction the light is coming from.
			light.setAzimuth((Math.toDegrees(Math.atan2(my, mx)) + 180) % 360);

			// Now we need to orient the gradient correctly. The gradient takes values (in
			// (x,y) form) ranging from 0 to 1, (0,0) specifying the top left corner of the
			// image, (.5,.5) being the middle, and (1,1) being the bottom right. Also the
			// gradient needs to start on the edge of the image and end on the edge of the
			// image. Our mx and my values are in coordinates ranging from -1 to 1, with
			// (0,0) being the point at the center.

			// First, let's make sure that our coordinates are on the edge of the image,
			// even if the mouse isn't.
			boolean x = Math.abs(mx) > Math.abs(my); // Find which component of the vector is greater.
			double scalefac = Math.abs(1 / (x ? mx : my)); // Determine how much to scale both components by, so that
															// the vector's tip lies on the image.

			// scalefac * mx and scalefac * my are the pieces of code that scale the
			// components.
			// Adding 1 to the result changes the range of possible values from -1,1 in both
			// axes to 0,2. Then we divide the values by 2, no matter what they are, so the
			// range then becomes 0,1. This is what we need for our linear gradient.
			double mxs = (scalefac * mx + 1) / 2, mxy = (scalefac * my + 1) / 2;

			mag *= .2;// Over here, we divide the magnitude by 5.

			// Here we specify the coordinates for the linear gradient.

			// First we specify the "FROM" coordinates, mxs and mxy (I just realized that
			// mxy should be named mys :})

			// Next, we need to specify the "TO" coordinates. Those coordinates should be
			// across the image, on its other edge, so all we need to do is perform a
			// reflection of the x and y values across their respective axes. To do that, we
			// simply subtract them from 1.
			ci.setPaint(new LinearGradient(mxs, mxy, 1 - mxs, 1 - mxy,
					// This "true" just means that the coordinates we specified above are
					// "proportional" so that they should range from 0 to 1, instead of from 0 to
					// whatever our image's size is. The NO_CYCLE value means that the gradient
					// shouldn't cycle (i.e. it shouldn't repeat).
					true, CycleMethod.NO_CYCLE,
					// Last, we need to specify the gradient's colors and their positions along the
					// gradient. We use "mag" for the starting position for the first, very dark
					// color. This means that if the magnitude is very high (meaning the mouse is
					// far away from the center of the image and that the image is rotated a lot),
					// the black "color stop" of the gradient is closer to the center of the
					// gradient (i.e. closer to 0.5), so more black appears visible, meaning more
					// shadow is visible.

					// We specify the second part of the gradient (transparency, at (0.18 * the
					// image's size) away from the previous color. This distance of 0.18*img size
					// will be filled with an interpolation between the two colors, providing the
					// "smoothness" between the black and transparent parts of the gradient.
					new Stop(mag, Color.gray(.02, 0.55)), new Stop(mag + 0.18, Color.TRANSPARENT)));
		});

		rollpivot.setOnMouseExited(event -> {
			pfiview.setRotate(0);// Undo any rotation when the mouse moves off the image.
			ci.setPaint(Color.TRANSPARENT);// Set the "gradient layer" to be invisible when the mouse is off. We don't
											// need a shadow when there's no mouse on the image.
		});

		selectedMenu.set(profileMenu);
		profileMenu.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				selectedMenu.set(profileMenu);
		});
		GUIUtils.applyClickAnimation(profileMenu, Color.GOLD, Color.RED, profileTitle);

	}

	private FileChooser fileChooser = new FileChooser();
	{
		fileChooser.setTitle("Select a profile icon");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
				new FileChooser.ExtensionFilter("PNG Image", "*.png"),
				new FileChooser.ExtensionFilter("JPEG Image", "*.jpg", "*.jpeg"),
				new FileChooser.ExtensionFilter("GIF Image", "*.gif"));
	}

	private @FXML void changePFI() {

		File f = fileChooser.showOpenDialog(app.getStage());
		if (f == null)
			return;
		Image i;
		try (FileInputStream is = new FileInputStream(f)) {
			i = new Image(is);
		} catch (IOException e) {
			Logging.err("Failed to load image: " + f);
			Logging.err(e);
			return;
		}
		if (i.isError()) {
			Logging.err("Failed to load image: " + f);
			Logging.err(i.getException());
			return;
		}
		try (FileInputStream is = new FileInputStream(f)) {
			app.getClient().getOwnUser().setProfileIcon(JavaTools.read(is));
		} catch (IOException | CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to send image.");
			Logging.err(e);
			return;
		}
	}

	private @FXML void removePFI() {
		try {
			app.getClient().getOwnUser().setProfileIcon(null);
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("An error occurred while contacting the server to remove your profile icon.");
			Logging.err(e);
		}
	}

	private @FXML void updateGeneralContent() {
		String un = usernamePrompt.getText().trim();

		final Border successBorder = FXTools.getBorderFromColor(Color.GREEN, 1);

		if (!un.isEmpty())
			if (!Utilities.isValidUsername(un))
				Logging.err("Invalid username.");
			else
				try {
					if (!un.equals(app.getClient().getOwnUser().usernameView().getValue())) {
						app.getClient().getOwnUser().setUsername(un);
						usernamePrompt.setBorder(successBorder);
					}
				} catch (CommunicationProtocolError | RuntimeException e) {
					Logging.err("An error occurred when setting the new username.");
					Logging.err(e);
				}

		String email = emailPrompt.getText().trim();
		if (!email.isEmpty())
			if (!Utilities.isValidEmail(email))
				Logging.err("Invalid email.");
			else
				try {
					if (!email.equals(app.getClient().getOwnUser().getEmail())) {
						app.getClient().getOwnUser().setEmail(email);
						emailPrompt.setBorder(successBorder);
					}
				} catch (CommunicationProtocolError | RuntimeException e) {
					Logging.err("An error occurred when setting the new email.");
				}

		String phone = phonePrompt.getText().trim();
		if (!phone.isEmpty())
			if (!Utilities.isValidPhoneNumber(phone))
				Logging.err("Invalid phone number.");
			else {
				try {
					if (!phone.equals(app.getClient().getOwnUser().getPhoneNumber())) {
						app.getClient().getOwnUser().setPhoneNumber(phone);
						phonePrompt.setBorder(successBorder);
					}
				} catch (CommunicationProtocolError | RuntimeException e) {
					Logging.err("An error occurred when setting the new phone number.");
				}
			}

	}

}
