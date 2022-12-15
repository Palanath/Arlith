package pala.apps.arlith.app.guis.home.center.friendstab;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import pala.apps.arlith.api.Utilities;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.app.application.Arlith;
import pala.apps.arlith.app.client.api.ClientUser;
import pala.apps.arlith.app.logging.Logging;
import pala.libs.generic.javafx.FXTools;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FriendListView extends TableView<ClientUser> {

	private static final Insets DEFAULT_ROW_PADDING = new Insets(1);
	private static final Border SELECTED_ROW_BORDER = FXTools.getBorderFromColor(Arlith.DEFAULT_ACTIVE_COLOR, 1),
			HOVER_ROW_BORDER = FXTools.getBorderFromColor(Arlith.DEFAULT_BASE_COLOR, 1);

	private static abstract class FriendsListCell extends TableCell<ClientUser, ClientUser> {
		/**
		 * Styles this {@link FriendsListCell} according to the provided
		 * {@link ClientUser} or <code>null</code> if the cell is to be empty. This
		 * method is called <b>before</b> the content of the cell is automatically
		 * updated, so calls to {@link #getItem()} will return the "old" item in this
		 * cell, not the new one being provided as the <code>value</code> parameter.
		 * 
		 * @param value The new {@link ClientUser} that this cell will render.
		 */
		public abstract void style(ClientUser value);

		{
			setTextAlignment(TextAlignment.CENTER);
			setAlignment(Pos.CENTER);
			setFont(Font.font(20));
			tableRowProperty().addListener((observable, oldValue, newValue) -> {
				textFillProperty().unbind();
				if (newValue != null)
					textFillProperty().bind(newValue.textFillProperty());
			});
		}

		@Override
		protected void updateItem(ClientUser item, boolean empty) {
			style(empty ? null : item);
			if (!isEmpty() && getItem() != null)
				((Map<ClientUser, FriendsListCell>) getTableColumn().getProperties().get(getTableColumn()))
						.remove(getItem());// Remove old.
			if (!empty && item != null)
				((Map<ClientUser, FriendsListCell>) getTableColumn().getProperties().get(getTableColumn())).put(item,
						this);// Add
			// new.
			super.updateItem(item, empty);
		}
	}

	private static final WritableImage MISSING_PFI_ICON = new WritableImage(2, 2);
	static {
		MISSING_PFI_ICON.getPixelWriter().setColor(0, 0, Color.BLACK);
		MISSING_PFI_ICON.getPixelWriter().setColor(0, 1, Color.MAGENTA);
		MISSING_PFI_ICON.getPixelWriter().setColor(1, 1, Color.BLACK);
		MISSING_PFI_ICON.getPixelWriter().setColor(1, 0, Color.MAGENTA);
	}

	private final TableColumn<ClientUser, ClientUser> pfps = new TableColumn<>("Icon"),
			names = new TableColumn<>("User Tag"), options = new TableColumn<>("Options");
	{
		pfps.setMinWidth(40);
		pfps.setPrefWidth(100);
		pfps.setMaxWidth(150);
		names.setMinWidth(200);
		options.setMinWidth(80);
		fixedCellSizeProperty().bind(pfps.widthProperty());
		setRowFactory(param -> {
			TableRow<ClientUser> r = new TableRow<ClientUser>() {
				{
					ChangeListener<? super Boolean> listener = (observable, oldValue, newValue) -> responsivelyStyle();
					selectedProperty().addListener(listener);
					hoverProperty().addListener(listener);
					emptyProperty().addListener(listener);
					setTextFill(Color.GOLD);
					setOnMouseClicked(event -> {
						if (isEmpty())
							getTableView().getSelectionModel().clearSelection();
					});
					prefHeightProperty().bind(pfps.widthProperty());
				}

				private void responsivelyStyle() {
					BORDER_HANDLER: if (!isEmpty()) {
						if (isSelected()) {
							setTextFill(Arlith.DEFAULT_ACTIVE_COLOR);
							setPadding(Insets.EMPTY);
							setBorder(SELECTED_ROW_BORDER);
						} else if (isHover()) {
							setTextFill(Arlith.DEFAULT_BASE_COLOR);
							setPadding(Insets.EMPTY);
							setBorder(HOVER_ROW_BORDER);
						} else
							break BORDER_HANDLER;
						return;
					}
					setTextFill(Arlith.DEFAULT_BASE_COLOR);
					setPadding(DEFAULT_ROW_PADDING);
					setBorder(null);
				}
			};
			r.setPadding(DEFAULT_ROW_PADDING);
			return r;
		});
		setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		getColumns().setAll(pfps, names, options);
		for (TableColumn<ClientUser, ClientUser> tc : (Iterable<TableColumn<ClientUser, ClientUser>>) (Iterable) this
				.getColumns()) {
			tc.getProperties().put(tc, new HashMap<>());
			tc.setCellValueFactory(param -> Bindings.createObjectBinding(param::getValue));
		}

		pfps.setCellFactory(param -> {
			ImageView iv = new ImageView();

			FriendsListCell cell = new FriendsListCell() {

				@Override
				public void style(ClientUser value) {
					if (value == null)
						iv.setImage(null);
					else
						try {
							iv.setImage(value.getProfileIcon());
						} catch (CommunicationProtocolError | RuntimeException e) {
							Logging.err("An error occurred while loading a user's icon.");
							Logging.err(e);
							iv.setImage(MISSING_PFI_ICON);
						}
				}
			};
			iv.fitHeightProperty().bind(pfps.widthProperty());
			iv.fitWidthProperty().bind(iv.fitHeightProperty());
			cell.setGraphic(iv);
			return cell;
		});
		names.setCellFactory(param -> new FriendsListCell() {
			@Override
			public void style(ClientUser value) {
				if (value == null)
					setText(null);
				else
					try {
						setText(value.getIdentifier());
					} catch (CommunicationProtocolError | RuntimeException e) {
						Logging.err("An error occurred while loading a user's tag.");
						Logging.err(e);
						setText("!#ERR");
					}
			}
		});
		options.setCellFactory(param -> {
			HBox box = new HBox();
			box.setSpacing(5);
			box.setAlignment(Pos.CENTER);
			FriendsListCell u = new FriendsListCell() {

				@Override
				public void style(ClientUser value) {
					if (value == null) {
						setGraphic(null);
						setText(null);
					} else
						try {
							if (!value.client().listFriends().contains(value))// Don't show anything for already
																				// friended users.
								if (value.client().getOutgoingFriendRequests().contains(value)) {// Show "rescind"
																									// button.
									Button rescind = new Button("Revoke");
									rescind.setOnAction(new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent event) {
											// TODO Revoke friend request.
											Logging.err("Not yet implemented... :-)");
										}
									});
									box.getChildren().setAll(rescind);
								} else {
									Button accept = new Button("Accept");
									accept.setOnAction(event -> {
										try {
											value.friend();
										} catch (CommunicationProtocolError | RuntimeException e) {
											Logging.err("An error occurred while trying to friend request the user.");
											Logging.err(e);
											FXTools.spawnLabelAtMousePos("Error; See Console (Ctrl + F3)",
													Color.FIREBRICK, FriendListView.this.getScene().getWindow());
											return;
										}
										// Restyle cell since we just updated the contents.
										Platform.runLater(() -> style(value));// Restyle with same ClientUser (but the
																				// ClientUser's properties were
																				// updated).
									});
									box.getChildren().setAll(accept);
								}
							else
								box.getChildren().clear();
							setText(null);
							setGraphic(box);
						} catch (CommunicationProtocolError | RuntimeException e) {
							Logging.err("An error occurred while checking the friend status of another user.");
							Logging.err(e);
							setText("Error");
							Utilities.runFX(() -> setGraphic(null));
						}
				}
			};
			u.setTextFill(Color.FIREBRICK);// Text is only used for errors.
			return u;
		});
	}

	public void restyle(ClientUser user) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> restyle(user));
		else
			for (TableColumn<?, ?> tc : getColumns())
				((Map<ClientUser, FriendsListCell>) tc.getProperties().get(tc)).get(user).style(user);
	}

}
