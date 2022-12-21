package pala.apps.arlith.frontend.server.database;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Denotes a method or constructor that modifies the database.
 * 
 * @author Palanath
 *
 */
@Documented
@Retention(SOURCE)
@Target({ METHOD, CONSTRUCTOR })
public @interface Writing {

}
