package pala.apps.arlith.frontend.clientgui.themes.gray.home;

import static pala.libs.generic.strings.StringTools.format;
import static pala.libs.generic.strings.StringTools.NumberUnit.SECONDS;

import java.math.BigInteger;

import javafx.beans.binding.Bindings;
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
import pala.libs.generic.strings.StringTools.NumberUnit;

public class UserListItem {

	private static final boolean geq(BigInteger first, NumberUnit second) {
		return first.compareTo(second.getAmt()) >= 0;
	}

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

	private static final NumberUnit YEARS = NumberUnit.YEARS.adjust(SECONDS),
			MONTHS = NumberUnit.MONTHS.adjust(SECONDS), WEEKS = NumberUnit.WEEKS.adjust(SECONDS),
			DAYS = NumberUnit.DAYS.adjust(SECONDS), HOURS = NumberUnit.HOURS.adjust(SECONDS),
			MINUTES = NumberUnit.MINUTES.adjust(SECONDS);
	{
		time.setFill(TIME_SINCE_LAST_MESSAGE_COLOR);
		Bindings.createStringBinding(() -> {
			BigInteger t = BigInteger.valueOf(getTimeSinceLastMessage());
			String res;
			if (geq(t, YEARS))
				// Show years & months.
				res = format(t, " ", YEARS, MONTHS);
			else if (geq(t, MONTHS))
				res = format(t, " ", MONTHS, DAYS);
			else if (geq(t, WEEKS))
				res = format(t, " ", WEEKS, DAYS);
			else if (geq(t, DAYS))
				res = format(t, " ", DAYS);
			else if (geq(t, HOURS))
				res = format(t, " ", HOURS, MINUTES);
			else if (geq(t, MINUTES))
				res = format(t, " ", MINUTES);
			else
				res = "<1m";
			return res + " ago";
		}, timeSinceLastMessageProperty());
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

	/**
	 * The time in seconds from the epoch since the last message in the channel
	 * associated with this {@link UserListItem} was sent.
	 */
	private final ObjectProperty<Long> timeSinceLastMessage = new SimpleObjectProperty<>();
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

	public final ObjectProperty<Long> timeSinceLastMessageProperty() {
		return this.timeSinceLastMessage;
	}

	public final Long getTimeSinceLastMessage() {
		return this.timeSinceLastMessageProperty().get();
	}

	public final void setTimeSinceLastMessage(final Long timeSinceLastMessage) {
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
