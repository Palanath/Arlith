package pala.apps.arlith.backend.client.requests.v2;

public interface StateAware {

	/**
	 * Determines whether this {@link StateAware} action has been queued to be
	 * executed or running. Completed actions return <code>true</code> for this.
	 * 
	 * @return <code>true</code> if this action has been queued on a
	 *         {@link RequestSubsystem}, <code>false</code> otherwise.
	 */
	boolean isQueued();

	/**
	 * Determines whether this {@link StateAware} action has been completed, either
	 * successfully or exceptionally.
	 * 
	 * @return <code>true</code> if this action has been completed and
	 *         <code>false</code> otherwise.
	 */
	boolean isComplete();
}
