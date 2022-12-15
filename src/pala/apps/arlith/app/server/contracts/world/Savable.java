package pala.apps.arlith.app.server.contracts.world;

import java.io.IOException;

public interface Savable {
	/**
	 * Saves this object to cold-storage.
	 *
	 * @throws IOException If an {@link IOException} occurs while saving.
	 */
	void save() throws IOException;
}
