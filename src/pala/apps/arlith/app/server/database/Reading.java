package pala.apps.arlith.app.server.database;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * <p>
 * Denotes code that is meant for reading/loading objects back from the
 * database, OR that it can be read or loaded from a database. Code with this
 * annotation is typically more exposed, modified in some way, or only present
 * to allow for the loading mechanism to work.
 * </p>
 * <p>
 * The loading mechanism of the server may require that modifiers to annotatable
 * elements be forgone, so this annotation allows for further specification of
 * what modifiers the annotated element would have, (i.e., what attributes it
 * would be declared with), had it not needed to be modified for the loading
 * mechanism, and thusly how the element should be treated to
 * non-loading-mechanism code. An example of this is a private field that can no
 * longer be initialized in the constructor due to the loading mechanism. The
 * field will need to be unmarked as final for the loading mechanism to do its
 * work, although should be treated as final to all non-loading-mechanism code.
 * The way that an annotated element should be treated is referred to as its
 * intended treatment.
 * </p>
 * 
 * @author Palanath
 *
 */
@Documented
@Retention(SOURCE)
public @interface Reading {
	/**
	 * Denotes the intended presence of the <code>final</code> modifier.
	 * 
	 * @return Whether this annotated type should be treated as <code>final</code>.
	 */
	boolean unmodifiable() default false;

	/**
	 * Denotes the intended accessibility level of the annotated element.
	 * 
	 * @return What accessibility level this annotated type should be treated as
	 *         possessing.
	 */
	Accessibility access() default Accessibility.NORMAL;

	enum Accessibility {
		/**
		 * Denotes that the actual accessibility modifier is the intended modifier.
		 */
		NORMAL,
		/**
		 * Denotes the intent of public accessibility.
		 */
		PUBLIC,
		/**
		 * Denotes the intent of protected accessiblity.
		 */
		PROTECTED,
		/**
		 * Denotes the intent of package-level accessibility which is formally indicated
		 * by the lack of an accessiblity modifier.
		 */
		PACKAGE,
		/**
		 * Denotes the intent of private accessibility.
		 */
		PRIVATE;
	}
}
