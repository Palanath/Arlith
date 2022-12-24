package pala.apps.arlith.frontend.guis.threadlistview;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pala.apps.arlith.application.JFXArlithRuntime;
import pala.apps.arlith.backend.client.api.ClientUser;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.events.IncomingFriendEvent;
import pala.apps.arlith.backend.common.protocol.types.FriendStateValue;
import pala.apps.arlith.frontend.guis.BindHandlerPage;
import pala.apps.arlith.frontend.guis.ArlithFrontend;
import pala.apps.arlith.frontend.guis.threadview.ThreadViewPage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.libs.generic.events.EventHandler;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.javafx.bindings.BindingTools;
import pala.libs.generic.javafx.bindings.BindingTools.FilterBinding;
import pala.libs.generic.strings.StringTools;
import pala.libs.generic.util.Box;

public class ThreadListViewPage extends BindHandlerPage {

	private static final Background THREAD_ENTRY_HOVER_BACKGROUND_COLOR = new Background(
			new BackgroundFill(new Color(1, 1, 1, 0.05), new CornerRadii(10), null));

	private final Map<ClientUser, ThreadEntry> threads = new HashMap<>();

	private @FXML Text title;
	private @FXML VBox threadbox;
	private @FXML TextField searchField;

	private ArlithWindow window;

	private final ObservableList<ThreadEntry> boxes = FXCollections.observableArrayList();
	private FilterBinding<?> entriesBinding;

	@Override
	public void cleanup(ArlithWindow window) {
		super.cleanup(window);
		entriesBinding.unbind();
		window.getApplication().getClient().unregister(IncomingFriendEvent.INCOMING_FRIEND_EVENT, newFriendHandler);
	}

	private final EventHandler<? super IncomingFriendEvent> newFriendHandler = event -> {
		ClientUser user = window.getApplication().getClient().getUser(event.getUser().getGid());
		if (event.getNewState() == FriendStateValue.FRIENDED) {
			try {
				ThreadEntry te = new ThreadEntry(user);
				threads.put(user, te);
				Platform.runLater(() -> boxes.add(te));
			} catch (CommunicationProtocolError | RuntimeException e3) {
				ArlithFrontend.getGuiLogger().err(
						"Failed to handle an incoming friend event involving user: " + event.getUser().getGid() + '.');
			}
		} else if (event.getPreviousState() == FriendStateValue.FRIENDED) {
			threadbox.getChildren().remove(threads.get(user));
		}
	};

	@Override
	public void show(ArlithWindow win) throws WindowLoadFailureException {
		win.getMenuBar().title.setText("THREADS");
		window = win;

		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		try {
			Parent node = loader.load(ThreadListViewPage.class.getResourceAsStream("ThreadListViewGUI.fxml"));
			node.getStylesheets().setAll("/pala/apps/arlith/app/guis/threadlistview/ThreadListViewStyles.css");
			win.setContent(node);
			try {
				for (ClientUser user : win.getApplication().getClient().listFriends()) {
					try {
						ThreadEntry te = new ThreadEntry(user);
						boxes.add(te);
						threads.put(user, te);
					} catch (CommunicationProtocolError | RuntimeException e) {
						ArlithFrontend.getGuiLogger().err("Failed to list the direct thread with user[id=" + user.id() + "].");
					}
				}
			} catch (CommunicationProtocolError | RuntimeException e) {
				ArlithFrontend.getGuiLogger().err("Failed to list friends.");
				ArlithFrontend.getGuiLogger().err(e);
			}
		} catch (IOException e) {
			throw new WindowLoadFailureException("Failed to load the thread list view.", e);
		}
		entriesBinding = BindingTools.filterBind1(boxes, threadbox.getChildren(), a -> searchField.getText().isEmpty()
				|| StringTools.containsIgnoreCase(a.getText(), searchField.getText().trim()));
		searchField.textProperty().addListener(a -> entriesBinding.refresh());
		win.getApplication().getClient().register(IncomingFriendEvent.INCOMING_FRIEND_EVENT, newFriendHandler);
	}

	private class ThreadEntry extends HBox {
		public String getText() {
			return threadname.getText();
		}

		private final ImageView cbg;
		private final Text threadname = new Text();
		private final StackPane icon = new StackPane();
		private final StackPane tnbox = new StackPane(threadname);
		{
//			cbg.setFill(Color.SADDLEBROWN);
			threadname.setFont(Font.font(20));
			tnbox.setAlignment(Pos.CENTER_LEFT);
			setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, CornerRadii.EMPTY,
					new BorderWidths(0, 0, 1, 0))));
			ArlithFrontend.applyClickAnimation(this, Color.GOLD, Color.RED, ArlithFrontend.CLICK_AND_RELEASE_ANIMATION_TIME,
					threadname);
			setSpacing(10);
			getChildren().setAll(icon, tnbox);
			hoverProperty().addListener((observable, oldValue,
					newValue) -> setBackground(newValue ? THREAD_ENTRY_HOVER_BACKGROUND_COLOR : null));
			setCursor(Cursor.HAND);
		}

		public ThreadEntry(ClientUser user) throws CommunicationProtocolError, RuntimeException {
			cbg = new ImageView();
			cbg.setFitWidth(64);
			cbg.setFitHeight(64);
			Box<Object> checker = new Box<>();
			user.getProfileIconRequest().then(t -> {
				synchronized (checker) {
					checker.value = new Object();
				}
				cbg.setImage(t);
			}).handle(t -> {
				try {
					ArlithFrontend.getGuiLogger().err("Failed to load a user's profile icon (User: " + user.getIdentifier() + ").");
				} catch (CommunicationProtocolError | RuntimeException e) {
					e.addSuppressed(t);
					e.printStackTrace();
					return;
				}
				t.printStackTrace();
			}).get();
			// Missing texture icon is loaded on first use, so if we don't need to, we don't
			// load it here.
			synchronized (checker) {
				if (checker.value == null)
					cbg.setImage(JFXArlithRuntime.MISSING_TEXTURE_IMAGE.get());
			}

			bindable(cbg::setImage).bind(user.profileIconView());// Bind user's profile icon.
			icon.getChildren().setAll(cbg);
			threadname.setText(user.getIdentifier());
			bindable(threadname::setText).bind(user.usernameView());// Bind user's username.
			setOnMouseClicked(event -> {
				if (event.getButton() == MouseButton.PRIMARY) {
					try {
						window.show(new ThreadViewPage(user.openDirectConversation()));
					} catch (CommunicationProtocolError | RuntimeException e1) {
						ArlithFrontend.getGuiLogger().err("Failed to open a direct thread with that user.");
						ArlithFrontend.getGuiLogger().err(e1);
						FXTools.spawnLabelAtMousePos("Error", Color.FIREBRICK, window.getApplication().getStage());
					} catch (WindowLoadFailureException e2) {
						ArlithFrontend.getGuiLogger().err("Failed to load the Thread Viewer window.");
						ArlithFrontend.getGuiLogger().err(e2);
						FXTools.spawnLabelAtMousePos("Error", Color.FIREBRICK, window.getApplication().getStage());
					}
				}
			});
		}

	}

}
