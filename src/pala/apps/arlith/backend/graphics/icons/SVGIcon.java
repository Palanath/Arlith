package pala.apps.arlith.backend.graphics.icons;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import pala.libs.generic.javafx.bindings.ListListener;

// SceneBuilder isn't recognizing the getSliceID and setSliceID functions. :(

public class SVGIcon extends Group {
	private final ObservableMap<String, Shape> slices = FXCollections.observableHashMap();

	public ObservableMap<String, Shape> getSlices() {
		return FXCollections.unmodifiableObservableMap(slices);
	}

	public Shape getSlice(String slice) {
		return slices.get(slice);
	}

	{
		getChildren().addListener(new ListListener<Node>() {

			@Override
			public void added(List<? extends Node> items, int startpos) {
				for (Node n : items)
					if (n instanceof Shape) {
						Shape slice = (Shape) n;
						if (slices.containsKey(getSliceID(slice)))
							throw new RuntimeException("Slice with same name already contained inside SVGIcon.");
						slices.put(getSliceID(slice), slice);
					}
			}

			@Override
			public void removed(List<? extends Node> items, int startpos) {
				for (Node n : items)
					if (n instanceof Shape)
						slices.remove(getSliceID(n));
			}
		});
	}

	private static final Object ID_KEY = new Object();

	public static String getSliceID(Node child) {
		return (String) child.getProperties().get(ID_KEY);
	}

	public static void setSliceID(Node child, String sliceID) {
		if (!(child instanceof Shape))
			return;
		if (child.getParent() instanceof SVGIcon) {
			SVGIcon parent = (SVGIcon) child.getParent();
			if (sliceID != null && parent.getSlice(sliceID) != null) // Check if new pos in parent is taken.
				throw new RuntimeException("Sibling node has same slice ID in target node's current parent.");
			parent.slices.remove(getSliceID(child));// Remove from curr pos in parent.
			if (sliceID != null)
				parent.slices.put(sliceID, (Shape) child);// Put in new pos.
		}
		if (sliceID != null)
			child.getProperties().put(ID_KEY, sliceID);
		else
			child.getProperties().remove(ID_KEY);
	}

}
