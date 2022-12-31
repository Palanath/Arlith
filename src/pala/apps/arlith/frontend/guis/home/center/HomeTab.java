package pala.apps.arlith.frontend.guis.home.center;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import pala.apps.arlith.backend.client.api.ClientMessage;
import pala.apps.arlith.backend.client.api.ClientThread;
import pala.apps.arlith.backend.client.api.notifs.ClientDirectMessageNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientFriendRequestNotification;
import pala.apps.arlith.backend.client.api.notifs.ClientNotification;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.types.FriendStateValue;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.guis.home.EventCard;
import pala.apps.arlith.frontend.guis.home.HomePage;

/**
 * @author Palanath
 *
 */
public class HomeTab extends PanelTab {

	private static final Color GRAY_CLOUD_COLOR = new Color(37d / 255, 37d / 255, 37d / 255, 1);
	private @FXML TilePane eventbox;
	private @FXML SVGPath cloudbg, cloudfg;
	private @FXML VBox txtbox;
	private @FXML AnchorPane acp;

	private final List<Cloud> clouds = new ArrayList<>();
	private final List<PathElement> pes = new ArrayList<PathElement>(29);
	{
		pes.add(new MoveTo(0, 0));
		{
			ArcTo arcTo = new ArcTo(8.13463, 9.3757544, 0, -7.783, 6.65408, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.7196201, 5.4397062, 0, -4.15038, -2.85071, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.7196201, 5.4397062, 0, -4.5612, 4.05686, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(6.062602, 6.987591, 0, -1.61648, -0.25349, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(6.062602, 6.987591, 0, -5.99166, 5.92618, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			HLineTo hLineTo = new HLineTo(-0.68477);
			hLineTo.setAbsolute(false);
			pes.add(hLineTo);
		}
		{
			HLineTo hLineTo = new HLineTo(-0.00015);
			hLineTo.setAbsolute(false);
			pes.add(hLineTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912495, 0, -7.36717, 8.49125, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912495, 0, 0.11636, 1.19042, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912495, 0, -7.0999, 8.45067, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912495, 0, 7.36721, 8.49124, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912495, 0, 0.00004, 0, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			HLineTo hLineTo = new HLineTo(71.98376);
			hLineTo.setAbsolute(false);
			pes.add(hLineTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912501, 0, 0.00008, 0, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912501, 0, 7.36714, -8.49124, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(7.3672133, 8.4912501, 0, -5.1456, -8.0516, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.9498459, 5.7050584, 0, 0.2725, -1.85483, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.9498459, 5.7050584, 0, -4.94984, -5.70505, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.9498459, 5.7050584, 0, -2.20029, 0.59702, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.9498463, 5.7050584, 0, -4.89832, -4.8869, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(4.9498463, 5.7050584, 0, -0.9257, 0.10475, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(6.3311978, 7.2971672, 0, 0.11993, -1.38726, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(6.3311978, 7.2971672, 0, -6.33123, -7.29719, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(6.3311978, 7.2971672, 0, -4.51865, 2.19271, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(8.13463, 9.3757544, 0, -5.57288, -2.54651, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(8.13463, 9.3757544, 0, -5.91869, 2.94742, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		{
			ArcTo arcTo = new ArcTo(8.13463, 9.3757544, 0, -7.51111, -5.77782, false, false);
			arcTo.setAbsolute(false);
			pes.add(arcTo);
		}
		pes.add(new ClosePath());
	}

	private class Cloud {

		private final Path path = new Path();

		{
			acp.getChildren().add(0, path);
			double r = Math.random();
			path.translateYProperty().bind(acp.heightProperty().multiply(r));
		}
		private final Transition t = new Transition() {
			{
				double time = Math.random() * 56 + 34;
				setCycleDuration(Duration.seconds(time));
				setCycleCount(INDEFINITE);
				setAutoReverse(true);
				jumpTo(Duration.seconds(Math.random() * time));
				setInterpolator(Interpolator.LINEAR);
			}

			@Override
			protected void interpolate(double frac) {
				path.setTranslateX(frac * acp.getWidth());
			}
		};
		{
			path.getElements().setAll(pes);

			path.setFill(GRAY_CLOUD_COLOR.interpolate(Color.GOLD, Math.random() * 0.2));
			path.setStroke(null);
			clouds.add(this);
			t.play();
		}
	}

	private void showEvents() {
		cloudbg.setVisible(false);
		cloudfg.setVisible(false);
		txtbox.setVisible(false);
	}

	private void showEmpty() {
		cloudbg.setVisible(true);
		cloudfg.setVisible(true);
		txtbox.setVisible(true);
	}

	public HomeTab(HomePage home) throws IOException {
		super("HomeTab.fxml", home, "Home");
		Map<GID, ClientNotification> notifs = home.getApp().getClient().getNotifications();
		if (notifs.isEmpty())
			showEmpty();
		else {
			for (ClientNotification n : notifs.values()) {
				if (n instanceof ClientFriendRequestNotification) {
					ClientFriendRequestNotification notif = (ClientFriendRequestNotification) n;
					String ident;
					try {
						ident = home.getApp().getClient().getUser(notif.getSourceUser()).getIdentifier();
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err("Failed to get user: " + notif.idHex()
								+ "'s user information (specifically the name). The connection to the server was lost and it could not be reopened. :(");
						continue;
					}
					StringBuilder sb = new StringBuilder(ident).append(' ');
					if (notif.getNewState() == FriendStateValue.FRIENDED)
						sb.append("accpted your friend request!");
					else if (notif.getNewState() == FriendStateValue.INCOMING)
						sb.append("sent you a friend request!");
					else
						sb.append("unfriended you.");
					eventbox.getChildren().add(EventCard.basic(ident, sb.toString(), EventCard.Type.FRIEND_REQUEST));
				} else if (n instanceof ClientDirectMessageNotification) {
					ClientDirectMessageNotification notif = (ClientDirectMessageNotification) n;
					String ident, content;
					try {
						ClientThread t = home.getApp().getClient().getThread(notif.getThread());
						ClientMessage msg = t.getMessage(notif.getMsg());
						ident = msg.getAuthor().getName();
						content = msg.getText();
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err(
								"Failed to query some information needed to show a notification from the server. (New DM notification for message: "
										+ notif.getMsg().getHex() + " in " + notif.getThread().getHex() + '.');
						continue;
					}
					eventbox.getChildren().add(EventCard.basic("DM From " + ident,
							content.substring(0, Math.min(content.length(), 256)), EventCard.Type.DIRECT_MESSAGE));
				}
			}
			showEvents();
		}

		Rectangle clip = new Rectangle(0, 0);
		clip.setArcHeight(40);
		clip.setArcWidth(40);
		clip.widthProperty().bind(acp.widthProperty());
		clip.heightProperty().bind(acp.heightProperty());
		acp.setClip(clip);
		for (int i = 0; i < 13; i++)
			new Cloud();
	}

}
