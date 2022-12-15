package pala.apps.arlith.app.server.contracts.world;

import java.io.OutputStream;

import pala.apps.arlith.api.streams.InputStream;

/**
 * Represents a store of data or the capability to transfer a specific set of
 * data.
 *
 * @author Palanath
 *
 */
public interface Icon {
	/**
	 * Returns an {@link InputStream} that reads over the bytes in this icon.
	 *
	 * @return An {@link InputStream} that contains this icon.
	 */
	InputStream getProfileIcon();

	// This is subject to change.
	/**
	 * Sets this icon to be the image contained within the provided
	 * {@link InputStream}.
	 *
	 * @param is An {@link InputStream} that, when read, returns the image data.
	 */
	void setProfileIcon(InputStream is);

	/**
	 * Writes this icon to the specified {@link OutputStream}.
	 *
	 * @param os The {@link OutputStream} to write the icon to.
	 */
	void writeProfileIcon(OutputStream os);
}
