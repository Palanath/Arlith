package pala.apps.arlith.backend.graphics.dialogs;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import pala.apps.arlith.frontend.guis.GUIUtils;

public class SimpleDialog extends Stage {

	{
		setResizable(false);
		initModality(Modality.APPLICATION_MODAL);
		GUIUtils.prepareStage(this);
	}

	public SimpleDialog(Window parent, Parent graphic) {
		initOwner(parent);
		Scene scene = new Scene(graphic);
		setScene(scene);
		if (parent.getScene() != null)
			scene.getStylesheets().setAll(parent.getScene().getStylesheets());
	}
}
