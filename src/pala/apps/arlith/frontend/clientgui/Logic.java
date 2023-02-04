package pala.apps.arlith.frontend.clientgui;

/**
 * Defines the function and user-communication features of a user interface.
 * 
 * @author Palanath
 *
 * @param <P> The type of presentation that can connect with the interface.
 */
public interface Logic<P extends Presentation<? extends Logic<? super P>>> {

}
