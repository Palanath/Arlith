package pala.apps.arlith.libraries.graphics.nodes;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class PromptField extends VBox {
	private final Text promptText = new Text(), errorText = new Text();
	private final TextField promptField = new TextField();
	private final DropShadow shadow = new DropShadow(10, Color.BLACK);
	{
		setSpacing(3);
		promptText.setTextAlignment(TextAlignment.LEFT);
		errorText.setTextAlignment(TextAlignment.RIGHT);
		StackPane sp = new StackPane(promptText, errorText);
		StackPane.setAlignment(promptText, Pos.CENTER_LEFT);
		StackPane.setAlignment(errorText, Pos.CENTER_RIGHT);
		getChildren().setAll(sp, promptField);
		promptField.prefWidthProperty().bind(prefWidthProperty());
	}

	public PromptField(String promptText) {
		this.promptText.setText(promptText);
	}

	public PromptField() {
	}

	public Text getPromptText() {
		return promptText;
	}

	public Text getErrorText() {
		return errorText;
	}

	public TextField getPromptField() {
		return promptField;
	}

	public void clearFlare() {
		errorText.setVisible(false);
		promptField.setEffect(null);
	}

	public void setFlareColor(Color color) {
		errorText.setVisible(true);
		errorText.setFill(color);
		shadow.setColor(color);
		promptField.setEffect(shadow);
	}

	public void flare(Color color) {
		setFlareColor(color);
	}

	public void flare(String text, Color color) {
		setFlareColor(color);
		errorText.setText(text);
	}

	public void setValid() {
		setValid("\u2713");
	}

	public void setValid(String text) {
		flare(text, Color.GREEN);
	}

	public void setWarning(String text) {
		flare(text, Color.GOLD);
	}

	public void setError(String text) {
		flare(text, Color.FIREBRICK);
	}

	public DropShadow getShadow() {
		return shadow;
	}

	public final String getText() {
		return promptField.getText();
	}

	public final void setText(String value) {
		promptField.setText(value);
	}

	public final StringProperty textProperty() {
		return promptField.textProperty();
	}

}
