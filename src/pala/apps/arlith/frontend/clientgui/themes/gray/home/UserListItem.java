package pala.apps.arlith.frontend.clientgui.themes.gray.home;

import java.time.Duration;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class UserListItem {

	private static final Color LAST_MESSAGE_COLOR = Color.gray(.5412),
			TIME_SINCE_LAST_MESSAGE_COLOR = Color.color(.21176470588, .55294117647, 0);

	private final ImageView icon = new ImageView();
	{
		icon.setFitWidth(32);
		icon.setFitHeight(32);
	}
	private final Label name = new Label();
	{
		name.setPrefWidth(100);
		name.setFont(Font.font(null, FontWeight.BOLD, 14));
	}
	private final StackPane spacer = new StackPane();
	{
		HBox.setHgrow(spacer, Priority.SOMETIMES);
	}
	private final Text time = new Text();
	{
		time.setFill(TIME_SINCE_LAST_MESSAGE_COLOR);
	}
	private final HBox header = new HBox(name, spacer, time);
	private final Label lastMessage = new Label();
	{
		lastMessage.setTextFill(LAST_MESSAGE_COLOR);
	}
	private final HBox lastMessageBox = new HBox(lastMessage);
	{
		VBox.setVgrow(lastMessageBox, Priority.SOMETIMES);
	}
	private final VBox userInformation = new VBox(header, lastMessageBox);
	{
		HBox.setHgrow(userInformation, Priority.SOMETIMES);
	}
	private final HBox root = new HBox(15, icon, userInformation);
	{
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(5));
		root.setPrefHeight(32);
	}

	/**
	 * The root element which contains the whole of this {@link UserListItem}'s
	 * graphical node hierarchy. This element can be added to another {@link Parent}
	 * to allow the {@link UserListItem} to be shown.
	 * 
	 * @return The root node of this {@link UserListItem}.
	 */
	public HBox getRoot() {
		return root;
	}

	private final ObjectProperty<Duration> timeSinceLastMessage = new SimpleObjectProperty<Duration>();
	// TODO Bind time's text property with a function of timeSinceLastMessage

	public final ObjectProperty<Image> profileIconProperty() {
		return icon.imageProperty();
	}

	public final Image getProfileIcon() {
		return this.profileIconProperty().get();
	}

	public final void setProfileIcon(final Image profileIcon) {
		this.profileIconProperty().set(profileIcon);
	}

	public final StringProperty usernameProperty() {
		return name.textProperty();
	}

	public final String getUsername() {
		return this.usernameProperty().get();
	}

	public final void setUsername(final String username) {
		this.usernameProperty().set(username);
	}

	public final ObjectProperty<Duration> timeSinceLastMessageProperty() {
		return this.timeSinceLastMessage;
	}

	public final Duration getTimeSinceLastMessage() {
		return this.timeSinceLastMessageProperty().get();
	}

	public final void setTimeSinceLastMessage(final Duration timeSinceLastMessage) {
		this.timeSinceLastMessageProperty().set(timeSinceLastMessage);
	}

	public final StringProperty previousMessageProperty() {
		return lastMessage.textProperty();
	}

	public final String getPreviousMessage() {
		return this.previousMessageProperty().get();
	}

	public final void setPreviousMessage(final String previousMessage) {
		this.previousMessageProperty().set(previousMessage);
	}

}
