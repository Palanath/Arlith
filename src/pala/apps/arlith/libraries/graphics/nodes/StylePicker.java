package pala.apps.arlith.libraries.graphics.nodes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import pala.libs.generic.javafx.FXTools;

public class StylePicker extends VBox {
	public interface OnSelect {
		void handle();
	}

	private OnSelect onSelect;

	public OnSelect getOnSelect() {
		return onSelect;
	}

	public void setOnSelect(OnSelect onSelect) {
		this.onSelect = onSelect;
	}

	private static final Color TEXT_BOX_BACKGROUND_COLOR = Color.gray(0.05, 0.7);

	public StylePicker() {
	}

	public StylePicker(OnSelect onSelect) {
		this.onSelect = onSelect;
	}

	private final Tab fontTab = new Tab("Font"), colorTab = new Tab("Color");
	private final TabPane pickers = new TabPane(fontTab, colorTab);
	private final FontPicker fontPicker = new FontPicker();
	private final AdvancedColorPicker colorPicker = new AdvancedColorPicker();
	private final Button doneButton = new Button("Insert Style");
	private final StackPane doneButtonContainer = new StackPane(doneButton);
	{
		doneButton.setOnAction(a -> onSelect.handle());
		doneButtonContainer.setMinHeight(50);
		colorPicker.setShowSelectButton(false);
		fontTab.setContent(fontPicker);
		colorTab.setContent(colorPicker);
		fontTab.setClosable(false);
		colorTab.setClosable(false);
		setStyle("-fx-border-width:0;");
		doneButton.getStyleClass().add("pop-button");
	}

	private final Text exampleText = new Text("Lorem Ipsum Dolor Sit Amet...");
	private final StackPane exampleTextWrapper = new StackPane(exampleText);
	private final ScrollPane textWrapper = new ScrollPane();
	{
		exampleText.fillProperty().bind(colorPicker.colorProperty());
		exampleText.underlineProperty().bind(fontPicker.underlinedProperty());
		exampleText.strikethroughProperty().bind(fontPicker.strikethroughProperty());
		exampleText.fontProperty().bind(Bindings.createObjectBinding(fontPicker::getFont, fontPicker.fontSizeProperty(),
				fontPicker.familyProperty(), fontPicker.boldProperty(), fontPicker.italicizedProperty()));
		getChildren().addAll(pickers, textWrapper, doneButtonContainer);

		StackPane box = new StackPane(exampleTextWrapper);
		box.setMinHeight(176);
		textWrapper.setContent(box);
		exampleTextWrapper.setBackground(FXTools.getBackgroundFromColor(TEXT_BOX_BACKGROUND_COLOR));
		exampleTextWrapper.setPadding(new Insets(8));
		textWrapper.setPadding(new Insets(12));
		textWrapper.setFitToWidth(true);
		textWrapper.setMaxHeight(200);
		textWrapper.setPrefHeight(200);
		textWrapper.setMaxWidth(462);// Prevent large fonts from causing issues when used in dialog and reopened.
										// [Issue: #140]

		setPadding(new Insets(0));
	}

	public FontPicker getFontPicker() {
		return fontPicker;
	}

	public AdvancedColorPicker getColorPicker() {
		return colorPicker;
	}

	public String getExampleText() {
		return exampleText.getText();
	}

	public void setExampleText(String text) {
		exampleText.setText(text);
	}

	public StringProperty exampleTextProperty() {
		return exampleText.textProperty();
	}

	public void showFontPicker() {
		pickers.getSelectionModel().select(fontTab);
	}

	public void showColorPicker() {
		pickers.getSelectionModel().select(colorTab);
	}

	public final ObjectProperty<FontWeight> boldProperty() {
		return fontPicker.boldProperty();
	}

	public String getFamily() {
		return fontPicker.getFamily();
	}

	public void setFamily(String family) {
		fontPicker.setFamily(family);
	}

	public double getFontSize() {
		return fontPicker.getFontSize();
	}

	public void setFontSize(double size) {
		fontPicker.setFontSize(size);
	}

	public Font getFont() {
		return fontPicker.getFont();
	}

	public final FontWeight getBold() {
		return fontPicker.getBold();
	}

	public Color getColor() {
		return colorPicker.getColor();
	}

	public double getHue() {
		return colorPicker.getHue();
	}

	public void setHue(double value) {
		colorPicker.setHue(value);
	}

	public double getBrightness() {
		return colorPicker.getBrightness();
	}

	public void setBrightness(double brightness) {
		colorPicker.setBrightness(brightness);
	}

	public double getSaturation() {
		return colorPicker.getSaturation();
	}

	public void setSaturation(double sat) {
		colorPicker.setSaturation(sat);
	}

	public double getOpacityValue() {
		return colorPicker.getOpacityValue();
	}

	public void setOpacityValue(double opacity) {
		colorPicker.setOpacityValue(opacity);
	}

	public DoubleProperty hueProperty() {
		return colorPicker.hueProperty();
	}

	public DoubleProperty saturationProperty() {
		return colorPicker.saturationProperty();
	}

	public void setColor(Color color) {
		colorPicker.setColor(color);
	}

	public ObjectProperty<Color> colorProperty() {
		return colorPicker.colorProperty();
	}

	public Color getHueColor() {
		return colorPicker.getHueColor();
	}

	public CheckBox getBoldCheckBox() {
		return fontPicker.getBoldCheckBox();
	}

	public CheckBox getItalicizedCheckBox() {
		return fontPicker.getItalicizedCheckBox();
	}

	public final void setBold(FontWeight bold) {
		fontPicker.setBold(bold);
	}

	public final ObjectProperty<FontPosture> italicizedProperty() {
		return fontPicker.italicizedProperty();
	}

	public final FontPosture getItalicized() {
		return fontPicker.getItalicized();
	}

	public final void setItalicized(FontPosture italicized) {
		fontPicker.setItalicized(italicized);
	}

	public final BooleanProperty strikethroughProperty() {
		return fontPicker.strikethroughProperty();
	}

	public final boolean isStrikethrough() {
		return fontPicker.isStrikethrough();
	}

	public final void setStrikethrough(boolean strikethrough) {
		fontPicker.setStrikethrough(strikethrough);
	}

	public final BooleanProperty underlinedProperty() {
		return fontPicker.underlinedProperty();
	}

	public final boolean isUnderlined() {
		return fontPicker.isUnderlined();
	}

	public final void setUnderlined(boolean underlined) {
		fontPicker.setUnderlined(underlined);
	}

}
