package pala.apps.arlith.frontend.clientgui;

/**
 * Defines the function and user-communication features of a user interface.
 * 
 * @author Palanath
 *
 * @param <P> The type of presentation that can connect with the interface.
 */
public interface Logic<P extends Presentation<? extends Logic<? super P>>> {
	/**
	 * <p>
	 * Hooks this {@link Logic} to the specified {@link Presentation} so that this
	 * {@link Logic} is aware of the {@link Presentation} through which the user is
	 * invoking triggers and submitting information, and to which the information
	 * that the logic needs to display is being presented.
	 * </p>
	 * <p>
	 * Most {@link Logic}s need this functionality when performing operations like
	 * querying data from their active {@link Presentation} to carry out a
	 * user-invoked operation.
	 * </p>
	 * <p>
	 * In the normal Logic-Presentation flow of creating and showing a scene, the
	 * {@link Logic} is instantiated first, and then each registered {@link Theme}
	 * is queried for a {@link Presentation} that can match with the {@link Logic}.
	 * If a {@link Theme} supports a specific scene and it provides such a
	 * {@link Presentation}, that {@link Presentation} will already be linked to the
	 * provided {@link Logic}. The {@link Logic} however, will not have been linked
	 * to the {@link Presentation} already. The {@link ClientGUIFrontend}, when
	 * showing a scene using its {@link ClientGUIFrontend#display(Logic)} method,
	 * obtains a {@link Presentation} that is already linked to the {@link Logic},
	 * and then calls {@link #hook(Presentation)} on the {@link Logic} to link the
	 * {@link Presentation}. If the {@link Logic} does not need the
	 * {@link Presentation}, it may define this method to do nothing.
	 * </p>
	 * 
	 * @param presentation The {@link Presentation} being linked with the
	 *                     {@link Logic}.
	 */
	void hook(P presentation);
}
