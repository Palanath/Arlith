package pala.apps.arlith.backend.graphics.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pala.libs.generic.javafx.FXTools;
import pala.libs.generic.javafx.bindings.BindingTools;
import pala.libs.generic.util.Gateway;

public class FontPicker extends GridPane {
	private static final Border INVALID_OPTION_BORDER = FXTools.getBorderFromColor(Color.FIREBRICK, 1, 14),
			VALID_OPTION_BORDER = FXTools.getBorderFromColor(Color.TRANSPARENT, 1, 14);
	private final CheckBox boldCheckBox = new CheckBox(), italicizedCheckBox = new CheckBox(),
			underlinedCheckBox = new CheckBox(), strikethroughCheckBox = new CheckBox();
	private final TextField fontSizeField = new TextField(), fontFamilyField = new TextField();

	private final DoubleProperty fontSize = new SimpleDoubleProperty();

	private static final List<String> families = new ArrayList<>(Font.getFamilies()), familiesLower = new ArrayList<>();
	static {
		for (String s : families)
			familiesLower.add(s.toLowerCase());
	}

	{
		Collections.sort(families);
		add(new Text("Bold:"), 0, 0);
		add(boldCheckBox, 1, 0);
		add(new Text("Italicize:"), 0, 1);
		add(italicizedCheckBox, 1, 1);
		add(new Text("Underline:"), 0, 2);
		add(underlinedCheckBox, 1, 2);
		add(new Text("Strikethrough:"), 0, 3);
		add(strikethroughCheckBox, 1, 3);

		add(new Text("Font Size:"), 0, 5);
		add(fontSizeField, 1, 5);
		add(new Text("Font Family:"), 0, 6);
		ComboBox<String> box = new ComboBox<>();
		box.setPromptText("...");
		HBox wrapper = new HBox(3, fontFamilyField, box);
		setFillWidth(wrapper, false);
		setHgrow(wrapper, Priority.NEVER);
		box.setPrefWidth(20);
		HBox.setHgrow(wrapper, Priority.NEVER);
		add(wrapper, 1, 6);

		fontSizeField.setText(String.valueOf(Font.getDefault().getSize()));
		fontFamilyField.setText(String.valueOf(Font.getDefault().getFamily()));
		box.getItems().addAll(families);
		fontFamilyField.setStyle("-fx-border:reset;-fx-border-radius:reset;");
		fontFamilyField.setBorder(VALID_OPTION_BORDER);
		fontSizeField.setStyle("-fx-border:reset;-fx-border-radius:reset;");
		fontSizeField.setBorder(VALID_OPTION_BORDER);

		box.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
			int index = fontFamilyField.getCaretPosition();
			fontFamilyField.setText(families.get(newValue.intValue()));
			fontFamilyField.positionCaret(index);
			fontFamilyField.setBorder(VALID_OPTION_BORDER);
		});
		fontFamilyField.setOnKeyReleased(event -> {
			int search = Collections.binarySearch(familiesLower, fontFamilyField.getText().toLowerCase());
			if (search >= 0) {
				box.getSelectionModel().select(search);
				fontFamilyField.setBorder(VALID_OPTION_BORDER);
			} else
				fontFamilyField.setBorder(INVALID_OPTION_BORDER);
		});

		setVgap(10);
		setHgap(20);
		setPadding(new Insets(20));

		BindingTools.bindBidirectional(fontSize, new Gateway<Number, String>() {

			@Override
			public String to(Number value) {
				return String.valueOf(value.doubleValue());
			}

			@Override
			public Number from(String value) {
				try {
					return Double.parseDouble(value);
				} catch (NumberFormatException e) {
					return -1;
				}
			}
		}, fontSizeField.textProperty());
	}

	private final ObjectProperty<FontWeight> bold = new SimpleObjectProperty<>(FontWeight.NORMAL);
	private final ObjectProperty<FontPosture> italicized = new SimpleObjectProperty<>(FontPosture.REGULAR);

	{
		bold.bind(BindingTools.mask(boldCheckBox.selectedProperty(), a -> a ? FontWeight.BOLD : FontWeight.NORMAL));
		italicized.bind(BindingTools.mask(italicizedCheckBox.selectedProperty(),
				a -> a ? FontPosture.ITALIC : FontPosture.REGULAR));
	}

	public CheckBox getBoldCheckBox() {
		return boldCheckBox;
	}

	public CheckBox getItalicizedCheckBox() {
		return italicizedCheckBox;
	}

	public CheckBox getUnderlinedCheckBox() {
		return underlinedCheckBox;
	}

	public CheckBox getStrikethroughCheckBox() {
		return strikethroughCheckBox;
	}

	public final ObjectProperty<FontWeight> boldProperty() {
		return bold;
	}

	public final FontWeight getBold() {
		return boldProperty().get();
	}

	public final void setBold(final FontWeight bold) {
		boldProperty().set(bold);
	}

	public final ObjectProperty<FontPosture> italicizedProperty() {
		return italicized;
	}

	public final FontPosture getItalicized() {
		return italicizedProperty().get();
	}

	public final void setItalicized(final FontPosture italicized) {
		italicizedProperty().set(italicized);
	}

	public final BooleanProperty strikethroughProperty() {
		return strikethroughCheckBox.selectedProperty();
	}

	public final boolean isStrikethrough() {
		return strikethroughProperty().get();
	}

	public final void setStrikethrough(final boolean strikethrough) {
		strikethroughProperty().set(strikethrough);
	}

	public final BooleanProperty underlinedProperty() {
		return underlinedCheckBox.selectedProperty();
	}

	public final boolean isUnderlined() {
		return underlinedProperty().get();
	}

	public final void setUnderlined(final boolean underlined) {
		underlinedProperty().set(underlined);
	}

	public String getFamily() {
		return fontFamilyField.getText();
	}

	public void setFamily(String family) {
		fontFamilyField.setText(family);
	}

	public StringProperty familyProperty() {
		return fontFamilyField.textProperty();
	}

	public double getFontSize() {
		return fontSize.get();
	}

	public void setFontSize(double size) {
		fontSize.set(size);
	}

	public DoubleProperty fontSizeProperty() {
		return fontSize;
	}

	public Font getFont() {
		return Font.font(getFamily(), getBold(), getItalicized(), getFontSize());
	}

}
