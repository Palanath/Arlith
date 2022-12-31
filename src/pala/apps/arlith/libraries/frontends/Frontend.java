package pala.apps.arlith.libraries.frontends;

import pala.apps.arlith.backend.client.ArlithClient;

/**
 * <p>
 * Represents a <b>Frontend</b> of the application. A Frontend consists of a set
 * of user interfaces, and their respective classes and resources, that let the
 * user interface with a specific backend component, or backend components, of
 * Arlith. Arlith can be configured via command-line arguments to run a
 * different Frontend when launched. The default Frontend in the end-user
 * edition of the application (that most are familiar with) is the JavaFX
 * GUI-based Client Frontend.
 * </p>
 * <p>
 * A Frontend typically possesses multiple <b>scenes</b>, each of which shows a
 * specific part, window, or set of GUI components to the user. For example, the
 * Client API (part of Arlith's backend) requires log in information to connect
 * to a server, so the GUI Client Frontend has a <i>log in</i> scene which gets
 * shown to the user as soon as the application launches. This scene is known as
 * the initial scene, since it is the first that the user is shown when the
 * Frontend is launched. Each Frontend scene is represented by an instance of
 * {@link FrontendScene}.
 * </p>
 * <p>
 * {@link FrontendScene}s typically rely on their {@link Frontend} class to
 * access state information that is shared between scenes. For example, the
 * JavaFX GUI-based Client Frontend's main {@link Frontend} class stores an
 * instance of {@link ArlithClient}, which each scene has access to. Each scene
 * can invoke operations on that object to query the cache or the server.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Frontend {
	/**
	 * Launches this {@link Frontend}, causing it to show its initial scene.
	 */
	void launch();
}
