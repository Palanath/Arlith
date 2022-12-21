package pala.apps.arlith.frontend.server.contracts.coldstorage.assetowner;

import java.io.File;

import pala.apps.arlith.frontend.server.contracts.coldstorage.ColdStorageObject;
import pala.apps.arlith.frontend.server.contracts.coldstorage.FilesystemStorageObject;

/**
 * <p>
 * Represents an object that possess assets, such as graphics or icons. These
 * assets are much like properties of {@link ColdStorageObject}s, but do not get
 * stored in a snapshot of the object, as they are external to the object and
 * must be restored or recorded separately.
 * </p>
 * <p>
 * {@link AssetOwner}s are {@link FilesystemStorageObject}s, not generic
 * {@link ColdStorageObject}s.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface AssetOwner extends FilesystemStorageObject {
	/**
	 * Returns the asset directory that this {@link AssetOwner} uses to store its
	 * assets on the filesystem.
	 * 
	 * @return The asset directory where this object stores its files.
	 */
	File getAssetDirectory();
}
