/**
 * This package contains classes and utilities that facilitate data reading and writing to physical memory.
 */
package pala.apps.arlith.app.server.database;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Documented
@Retention(SOURCE)
/**
 * Indicates that code does not make use of the {@link Database} API.
 * 
 * @author Palanath
 *
 */
public @interface DBless {

}
