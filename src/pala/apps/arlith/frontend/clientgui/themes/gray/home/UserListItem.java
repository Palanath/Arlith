package pala.apps.arlith.frontend.clientgui.themes.gray.home;

import java.time.Duration;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

	private final ObjectProperty<Image> profileIcon = icon.imageProperty();
	private final StringProperty username = name.textProperty();
	private final ObjectProperty<Duration> timeSinceLastMessage = new SimpleObjectProperty<Duration>();
	// TODO Bind time's text property with a function of timeSinceLastMessage
	private final StringProperty previousMessage = lastMessage.textProperty();
}
