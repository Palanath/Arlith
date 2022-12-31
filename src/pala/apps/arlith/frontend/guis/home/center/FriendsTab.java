package pala.apps.arlith.frontend.guis.home.center;

import java.io.IOException;
import java.math.BigInteger;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import pala.apps.arlith.backend.client.api.ClientUser;
import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.server.contracts.world.ServerUser.FriendState;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.guis.home.HomePage;
import pala.apps.arlith.frontend.guis.home.center.friendstab.FriendListView;
import pala.apps.arlith.libraries.Utilities;
import pala.apps.arlith.libraries.graphics.nodes.PromptField;
import pala.libs.generic.events.EventHandler;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.strings.StringTools;

public class FriendsTab extends PanelTab {

	private final ObservableList<ClientUser> users = FXCollections.observableArrayList();

	private @FXML VBox friendsListBox;
	private @FXML TilePane normalFriendFilter, multiFriendFilter;
	private final FriendListView allTab = new FriendListView();
	{
		normalFriendFilter.visibleProperty().bind(multiFriendFilter.visibleProperty().not());
		allTab.setItems(users);
		friendsListBox.getChildren().add(allTab);
		VBox.setVgrow(allTab, Priority.SOMETIMES);
	}
	private @FXML VBox friendRequestPromptContainer, friendRequestSection;
	private PromptField friendRequestPrompt = new PromptField("User ID/Tag:");
	{
		friendRequestPromptContainer.getChildren().add(1, friendRequestPrompt);
		friendRequestPrompt.setPrefWidth(500);
		friendRequestPrompt.textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> {
			if (newValue.isEmpty())
				friendRequestPrompt.clearFlare();
			else {
				String txt = friendRequestPrompt.getText();
				CHECK: {

					// Check if GID.
					try {
						if (StringTools.fromHexString(txt).length == 20)
							break CHECK;
					} catch (NumberFormatException e) {
					}
					try {
						if (new BigInteger(txt).toByteArray().length == 20)
							break CHECK;
					} catch (IllegalArgumentException e2) {
					}

					// Not a GID; check if tag.

					int hashtagPos = txt.indexOf('#');
					if (hashtagPos == -1) {
						friendRequestPrompt.setError("Tag missing '#'");
						return;
					}
					if (hashtagPos < 3) {
						friendRequestPrompt.setError("Username too short (Username#Disc)");
						return;
					}

					String username = txt.substring(0, hashtagPos);
					if (!Utilities.isValidUsername(username)) {
						friendRequestPrompt.setError("Username illformed (Username#Disc)");
						return;
					}

					String disc = txt.substring(hashtagPos + 1);
					if (disc.isEmpty()) {
						friendRequestPrompt.setError("Disc can't be empty (Username#Disc)");
						return;
					}
					if (disc.length() < 3) {
						friendRequestPrompt.setError("Disc too short (Username#Disc)");
						return;
					}
					if (!StringTools.isNumeric(disc)) {
						friendRequestPrompt.setError("Disc must be numeric (Username#Disc)");
						return;
					}

				}
				friendRequestPrompt.setValid();
			}
		});
	}

	private @FXML void sendFriendRequest() {
		if (friendRequestPrompt.getText().isEmpty())
			friendRequestPrompt.setError("Enter a user's ID/Tag");
		else {
			String t = friendRequestPrompt.getText();
			int htind = t.indexOf('#');
			if (htind != -1) {
				String name = t.substring(0, htind);
				String disc = t.substring(htind + 1, t.length());
				if (name.isEmpty() || disc.isEmpty())
					FXTools.spawnLabelAtMousePos("Name and disc cannot be empty. (Name#disc)", Color.FIREBRICK,
							home.getWindow().getApplication().getStage());
				else {

					GID id;
					try {
						id = home.getApp().getClient().friend(name, disc);
					} catch (CommunicationProtocolError | RuntimeException e) {
						FXTools.spawnLabelAtMousePos("An error occurred.", Color.FIREBRICK,
								home.getWindow().getApplication().getStage());
						ArlithFrontend.getGuiLogger().err(e);
						return;
					}
					FXTools.spawnLabelAtMousePos("Friend Request Sent", Color.GREEN,
							home.getWindow().getApplication().getStage());
					try {
						for (ClientUser u : home.getApp().getClient().listFriends())
							if (u.id().equals(id)) {
								updateState(u, FriendState.FRIENDED);
								return;
							}
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err("Failed to check if user being friend requested is already friended.");
						ArlithFrontend.getGuiLogger().err(e);
						// If this fails, don't return; still try to friend the user and continue. The
						// rendering part won't work, but the friend request will still go through.
					}
					try {
						for (ClientUser u : home.getApp().getClient().getOutgoingFriendRequests())
							if (u.id().equals(id)) {
								updateState(u, FriendState.FRIEND_REQUESTED);
								return;
							}
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err(
								"Failed to check if pending friend request already exists for user being friend requested.");
						ArlithFrontend.getGuiLogger().err(e);
						// If this part fails, we just want to continue on and send the friend request
						// anyway, in case the person being friend requested is not already friend
						// requested.
					}
					friendRequestPrompt.getPromptField().clear();
				}
			} else {
				GID gid;
				try {
					gid = GID.fromHex(t);
				} catch (NumberFormatException e) {
					try {
						gid = GID.fromString(t);
					} catch (Exception e2) {
						FXTools.spawnLabelAtMousePos(
								"Invalid GID. Please enter a GID or a name followed by a discriminator.",
								Color.FIREBRICK, home.getWindow().getApplication().getStage());
						return;
					}
				}

				try {
					for (ClientUser u : home.getApp().getClient().listFriends())
						if (u.id().equals(gid)) {
							FXTools.spawnLabelAtMousePos(u.getIdentifier() + " already friended.", Color.GOLD,
									home.getApp().getStage());
							return;
						}
				} catch (CommunicationProtocolError | RuntimeException e) {
					ArlithFrontend.getGuiLogger().err("Failed to check if user being friend requested is already friended.");
					ArlithFrontend.getGuiLogger().err(e);
					// If this fails, don't return; still try to friend the user and continue. The
					// rendering part won't work, but the friend request will still go through.
				}
				try {
					for (ClientUser u : home.getApp().getClient().getOutgoingFriendRequests())
						if (u.id().equals(gid)) {
							FXTools.spawnLabelAtMousePos(
									"Friend request for " + u.getIdentifier() + " already sent.", Color.GOLD,
									home.getApp().getStage());
							return;
						}
				} catch (CommunicationProtocolError | RuntimeException e) {
					ArlithFrontend.getGuiLogger().err(
							"Failed to check if pending friend request already exists for user being friend requested.");
					ArlithFrontend.getGuiLogger().err(e);
					// If this part fails, we just want to continue on and send the friend request
					// anyway, in case the person being friend requested is not already friend
					// requested.
				}

				try {
					home.getApp().getClient().friend(gid);
				} catch (CommunicationProtocolError | RuntimeException e) {
					FXTools.spawnLabelAtMousePos("An error occurred.", Color.FIREBRICK,
							home.getWindow().getApplication().getStage());
					ArlithFrontend.getGuiLogger().err(e);
					return;
				}
				FXTools.spawnLabelAtMousePos("Friend Request Sent", Color.GREEN,
						home.getWindow().getApplication().getStage());
				RESOLVE_FRIEND_STATE: {
					try {
						for (ClientUser u : home.getApp().getClient().getIncomingFriendRequests())
							if (u.id().equals(gid)) {
								// If there was an incoming friend request for the user, then by sending this
								// friend request, the currently logged in user "accepted" the other person's
								// request. We make the GUI reflect this.
								updateState(u, FriendState.FRIENDED);
								break RESOLVE_FRIEND_STATE;
							}
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err("Failed to check friend status of the user being friend requested.");
						ArlithFrontend.getGuiLogger().err(e);
					}
					// User was not found in incoming list, so there's now only an outgoing friend
					// request. We make the GUI reflect that.
					updateState(home.getApp().getClient().getUser(gid), FriendState.FRIEND_REQUESTED);
				}
				friendRequestPrompt.getPromptField().clear();
			}
		}
	}

	private final javafx.event.EventHandler<KeyEvent> ctrlPressHandler = event -> {
		if (event.getCode() == KeyCode.CONTROL)
			if (event.getEventType() == KeyEvent.KEY_PRESSED)
				multiFriendFilter.setVisible(true);
			else if (event.getEventType() == KeyEvent.KEY_RELEASED)
				multiFriendFilter.setVisible(false);
	};

	public FriendsTab(HomePage home) throws IOException {
		super("FriendsTab.fxml", home, "Friends");
		home.getApp().getStage().addEventHandler(KeyEvent.KEY_PRESSED, ctrlPressHandler);
		home.getApp().getStage().addEventHandler(KeyEvent.KEY_RELEASED, ctrlPressHandler);
	}

	public void destroy() {
		home.getApp().getClient().unregister(IncomingFriendEvent.INCOMING_FRIEND_EVENT, friendEventHandler);
		home.getApp().getStage().removeEventHandler(KeyEvent.KEY_PRESSED, ctrlPressHandler);
		home.getApp().getStage().removeEventHandler(KeyEvent.KEY_RELEASED, ctrlPressHandler);
	}

	private void updateState(ClientUser user, FriendState newState) {
		if (newState == null)
			users.remove(user);
		else if (users.contains(user))
			allTab.restyle(user);
		else
			users.add(user);
	}

	private final EventHandler<? super IncomingFriendEvent> friendEventHandler = event -> {
		ClientUser user = home.getApp().getClient().getUser(event.getUser().getGid());
		updateState(user, FriendState.fromCommunicationProtocolState(event.getNewState()));
	};

	{
		home.getApp().getClient().register(IncomingFriendEvent.INCOMING_FRIEND_EVENT, friendEventHandler);
		try {
			for (ClientUser u : home.getApp().getClient().getIncomingFriendRequests())
				updateState(u, FriendState.INCOMING_REQUEST);
		} catch (CommunicationProtocolError | RuntimeException e) {
			ArlithFrontend.getGuiLogger().err("Failed to list incoming friend requests.");
			ArlithFrontend.getGuiLogger().err(e);
		}
		try {
			for (ClientUser u : home.getApp().getClient().listFriends())
				updateState(u, FriendState.FRIENDED);
		} catch (CommunicationProtocolError | RuntimeException e) {
			ArlithFrontend.getGuiLogger().err("Failed to list friends.");
			ArlithFrontend.getGuiLogger().err(e);
		}
		try {
			for (ClientUser u : home.getApp().getClient().getOutgoingFriendRequests())
				updateState(u, FriendState.FRIEND_REQUESTED);
		} catch (CommunicationProtocolError | RuntimeException e) {
			ArlithFrontend.getGuiLogger().err("Failed to list outgoing friend requests.");
			ArlithFrontend.getGuiLogger().err(e);
		}
	}

}
