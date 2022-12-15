package pala.apps.arlith.app.server.contracts.coldstorage;

/**
 * Represents an object that can be stored in cold-storage. {@link #save()} can
 * be called to save such an object.
 * 
 * @author Palanath
 *
 */
public interface ColdStorageObject {
	/**
	 * Saves the state of this object to cold-storage in this object's designated
	 * cold-storage location.
	 */
	void save();

	/**
	 * <p>
	 * Restores the state of this object from cold-storage. If data does not
	 * currently exist in this object's designated cold-storage location, this
	 * method returns <code>false</code>.
	 * </p>
	 * <p>
	 * If the state in cold-storage is malformed, or is valid but would leave this
	 * object in an invalid or inconsistent state at the time of restoration, this
	 * method throws an {@link IllegalArgumentException}.
	 * </p>
	 * 
	 * @throws IllegalArgumentException If the state in cold-storage could not be
	 *                                  restored because it is either invalid or
	 *                                  because it would leave this object in an
	 *                                  invalid or inconsistent state.
	 */
	boolean restore() throws IllegalArgumentException;
}
