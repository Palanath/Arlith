package pala.apps.arlith.app.application;

import javafx.scene.image.Image;
import pala.libs.generic.items.LateLoadItem;

public class ArlithResources {
	public static final LateLoadItem<Image> MISSING_TEXTURE_IMAGE = new LateLoadItem<>(
			() -> new Image("/pala/apps/arlith/missing-texture.png", false));
}
