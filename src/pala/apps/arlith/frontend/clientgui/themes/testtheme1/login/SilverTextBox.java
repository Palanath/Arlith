package pala.apps.arlith.frontend.clientgui.themes.testtheme1.login;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import pala.libs.generic.javafx.FXTools;

public class SilverTextBox extends VBox {
	private final Text prompt = new Text();
	private final TextField input = new TextField();
	private final Line line = new Line();
	{
		prompt.setFill(Color.BLACK);
		getChildren().addAll(prompt, input, line);
		input.setBackground(FXTools.getBackgroundFromColor(Color.gray(.8, .3)));
		line.setStrokeWidth(3);
		line.setFill(Color.WHITE);
		line.setVisible(false);
	}
}
