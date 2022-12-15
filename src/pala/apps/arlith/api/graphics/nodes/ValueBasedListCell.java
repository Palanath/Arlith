package pala.apps.arlith.api.graphics.nodes;

import javafx.scene.control.ListCell;

/**
 * <p>
 * A {@link ListCell} that updates its graphical style whenever the element it
 * contains changes. This class acts as a convenience class for
 * {@link ListCell}s that simply render themselves based on their value. The
 * {@link #style(Object)} method is called automatically whenever the element
 * contained within the {@link ValueBasedListCell} is changed (or removed), and
 * the {@link ValueBasedListCell} can update its style accordingly.
 * </p>
 * <p>
 * This class is purely for convenience, and other styling change-listeners or
 * handlers may be specified as would be done on any normal {@link ListCell}.
 * </p>
 * 
 * @author Palanath
 *
 * @param <T> The type of value contained by the {@link ValueBasedListCell}.
 */
public abstract class ValueBasedListCell<T> extends ListCell<T> {
	protected abstract void style(T value);

	{
		itemProperty().addListener((observable, oldValue, newValue) -> style(newValue));
	}
}
