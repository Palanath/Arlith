package pala.apps.arlith.app.client.api.properties;

import java.util.function.Supplier;

import pala.apps.arlith.app.client.api.ClientObject;
import pala.libs.generic.items.LateLoadItem;

public class ObjectProperty<T extends ClientObject> extends LateLoadItem<T> {

	public ObjectProperty(Supplier<T> supplier) {
		super(supplier::get);
	}

}
