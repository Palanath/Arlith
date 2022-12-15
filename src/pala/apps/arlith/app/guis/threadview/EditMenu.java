package pala.apps.arlith.app.guis.threadview;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import pala.libs.generic.javafx.bindings.BindingTools;

class EditMenu extends Menu {
	private final MenuItem copy = new MenuItem("Copy"), cut = new MenuItem("Cut"), undo = new MenuItem("Undo"),
			redo = new MenuItem("Redo"), paste = new MenuItem("Paste"), selectAll = new MenuItem("Select All");

	{
		getItems().addAll(copy, cut, undo, redo, paste, selectAll);
	}

	public MenuItem getCopy() {
		return copy;
	}

	public MenuItem getCut() {
		return cut;
	}

	public MenuItem getUndo() {
		return undo;
	}

	public MenuItem getRedo() {
		return redo;
	}

	public MenuItem getPaste() {
		return paste;
	}

	public MenuItem getSelectAll() {
		return selectAll;
	}

	public EditMenu(TextInputControl control) {
		super("Edit");
		ObservableValue<Boolean> nothingSelectedProperty = BindingTools.mask(control.selectionProperty(),
				a -> a.getLength() == 0);
		copy.disableProperty().bind(nothingSelectedProperty);
		cut.disableProperty().bind(nothingSelectedProperty);

		undo.disableProperty().bind(control.undoableProperty().not());
		redo.disableProperty().bind(control.redoableProperty().not());

		// Paste disabledness needs to be handled by the developer

		copy.setOnAction(a -> control.copy());
		cut.setOnAction(a -> control.cut());
		undo.setOnAction(a -> control.undo());
		redo.setOnAction(a -> control.redo());
		paste.setOnAction(a -> control.paste());
		selectAll.setOnAction(a -> control.selectAll());
	}

}
