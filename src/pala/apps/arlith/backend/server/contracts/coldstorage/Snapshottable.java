package pala.apps.arlith.backend.server.contracts.coldstorage;

import pala.apps.arlith.backend.server.contracts.world.ServerUser;
import pala.apps.arlith.backend.server.contracts.world.ServerWorld;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * An object whose state can be recorded to a {@link JSONObject} at any time.
 * {@link Snapshottable}s permit restoration of state from such {@link JSONObject}
 * snapshots through {@link #restore(JSONObject)}, and can be constructed from
 * snapshots as well.
 * </p>
 * <p>
 * Implementations should possess a constructor that takes a {@link JSONObject}
 * argument. The constructor should construct the instance to have the state
 * represented by the {@link JSONObject}, if such a state is valid. Otherwise,
 * the constructor should throw an {@link IllegalArgumentException}.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Snapshottable {

	/**
	 * Returns a {@link JSONObject} that stores all of the properties of this
	 * {@link Snapshottable}. The {@link JSONObject} stores all the necessary data
	 * needed for a {@link ServerWorld} to recover this {@link Snapshottable} either during
	 * runtime or startup. It also serves as a snapshot of this {@link Snapshottable}
	 * at a given point in time, and so can be used to update (or rather, revert)
	 * the state of a Java {@link Snapshottable} that represents the same object as it
	 * through {@link #restore(JSONObject)} at any point in the future.
	 * 
	 * @return A {@link JSONObject} representing this {@link Snapshottable}.
	 */
	JSONObject snapshot();

	/**
	 * <p>
	 * Modifies this {@link Snapshottable} to have the same properties as stored in the
	 * provided {@link JSONObject} snapshot of this object. This method performs an
	 * <i>update</i> operation on an already-existing Java object.
	 * </p>
	 * <p>
	 * This method must check to make sure that the provided snapshot represents
	 * this <i>same</i> conceptual object. If it does not, this method should throw
	 * an {@link IllegalArgumentException} and reject the argument, without
	 * modifying this {@link Snapshottable} at all.
	 * </p>
	 * <p>
	 * Furthermore, this method must also verify that the state of the snapshot is
	 * of a valid state for this object to be in at the point in time that this
	 * method is to apply the restoration. If not the case, this method should throw
	 * an {@link IllegalArgumentException}, or a subclass thereof.
	 * </p>
	 * <h1>Object Matching</h1>
	 * <p>
	 * Snapshots should be matched against this {@link Snapshottable} to assert that
	 * they represent a snapshot of the state of a {@link Snapshottable} that
	 * represents the same {@link Snapshottable} as this one. For example, a
	 * {@link ServerUser} implementation may compare the {@link ServerUser#getGID() GID} of
	 * the {@link ServerUser} with the one stored in the provided snapshot to assure
	 * that the snapshot is a snapshot of <i>that</i> {@link ServerUser} before
	 * performing the update.
	 * </p>
	 * <p>
	 * In contrast, when loading a {@link Snapshottable} from a {@link JSONObject},
	 * rather than updating/restoring one's state, the {@link Snapshottable} is
	 * constructed to have the same state and properties as the provided
	 * {@link JSONObject}, and no comparison needs to be made initially.
	 * </p>
	 * <p>
	 * A {@link Snapshottable} can only be constructed and loaded from a
	 * {@link JSONObject} once, but can have its state restored through this method
	 * many times.
	 * </p>
	 * 
	 * @param snap The snapshot to update the state of this {@link Snapshottable} with.
	 * @throws IllegalArgumentException If the provided snapshot is either not a
	 *                                  valid snapshot for this object or if it
	 *                                  depicts an invalid state for this object to
	 *                                  be in at the time the restoration is to
	 *                                  execute.
	 */
	void restore(JSONObject snap) throws IllegalArgumentException;
}
