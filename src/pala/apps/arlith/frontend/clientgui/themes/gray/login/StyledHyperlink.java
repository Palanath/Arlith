package pala.apps.arlith.frontend.clientgui.themes.gray.login;

import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.paint.Color;

public class StyledHyperlink extends Hyperlink {
	{
		setTextFill(Color.BLUE);
		textFillProperty().bind(
				Bindings.createObjectBinding(() -> isArmed() ? Color.RED : isVisited() ? Color.gray(.18) : Color.BLUE,
						armedProperty(), visitedProperty()));
		setBorder(null);
	}

	public StyledHyperlink() {
	}

	public StyledHyperlink(String text, Node graphic) {
		super(text, graphic);
	}

	public StyledHyperlink(String text) {
		super(text);
	}

}
