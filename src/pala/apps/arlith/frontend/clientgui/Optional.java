/**
 * 
 */
package pala.apps.arlith.frontend.clientgui;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target(METHOD)
/**
 * <p>
 * Indicates that a method is optional, and that its implementation is not
 * required for a base implementation of a presentation.
 * </p>
 * <p>
 * A {@link Logic} subinterface will use this on one of its methods to indicate
 * that that method does not need to be invoked by every corresponding
 * presentation implementation. The annotation includes a {@link #type()}, which
 * specifies the specific {@link Presentation} subinterface that corresponding
 * presentation implementations for should handle.
 * </p>
 * <p>
 * For example, consider the {@link LogInLogic} and corresponding
 * {@link LogInPresentation} interfaces. The {@link LogInLogic} declares
 * {@link LogInLogic#triggerCheckUsername()}, which is annotated with
 * {@link Optional} such that {@link Optional#type()} is
 * {@link LogInPresentationWithLiveInputResponse}. This indicates to
 * presentations attempting to interface with {@link LogInLogic} that they only
 * need to invoke {@link LogInLogic#triggerCheckUsername()} if they are an
 * instance of {@link LogInPresentationWithLiveInputResponse}, i.e., concrete
 * direct subclasses of {@link LogInPresentation} do not need to trigger that
 * method. This is because {@link LogInLogic#triggerCheckUsername()} is designed
 * to be called by presentations which present the user with live feedback, and
 * are thus implementations of {@link LogInPresentationWithLiveInputResponse}.
 * Normal presentation implementations (that do not provide immediate feebdack)
 * are not expected to do this.
 * </p>
 * <p>
 * This annotation is designed to be applied to methods inside the {@link Logic}
 * of a user interface.
 * </p>
 * 
 * @author Palanath
 *
 */
public @interface Optional {
	/**
	 * The type of presentation that the annotated method is designed to work with.
	 * By default, this is {@link Presentation#getClass() Presentation.class}, which
	 * indicates that the annotated method is <i>completely</i> optional for all
	 * presentations to use. If another, more specific type is specified, then
	 * {@link Presentation}s that are an instance of that type should trigger or
	 * interface with the annotated method.
	 * 
	 * @return The {@link Presentation} type that should interface with the
	 *         annotated method, or {@link Presentation#getClass()
	 *         Presentation.class} if interfacing with this method is entirely
	 *         optional.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends Presentation> type() default Presentation.class;
}
