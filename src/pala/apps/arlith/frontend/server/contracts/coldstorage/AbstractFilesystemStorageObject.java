package pala.apps.arlith.frontend.server.contracts.coldstorage;

import java.io.File;

public interface AbstractFilesystemStorageObject extends ColdStorageObject {
	/**
	 * Gets the {@link File} that the properties of the state of this object gets
	 * stored in.
	 * 
	 * @return The {@link File} designated for this object for storage.
	 */
	File getStorageFile();
}
