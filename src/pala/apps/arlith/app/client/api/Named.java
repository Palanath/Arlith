package pala.apps.arlith.app.client.api;

import pala.apps.arlith.app.client.Client;

/**
 * Interface for all {@link Client} API objects types that have a
 * {@link #name()}. This interface isn't used much now, but exists so that
 * application code can refer to a type that encompasses all named Arlith client
 * API objects.
 * 
 * @author Palanath
 *
 */
public interface Named {
	String name();
}
