/**
 * <p>
 * Stores code defining <i>interfaces</i> between the application and the user.
 * An interface specifies the input and output features that a
 * {@link pala.apps.arlith.libraries.frontends.Frontend}'s scene supports.
 * </p>
 * <p>
 * The presentation of the UI to the user must satisfy these for the user to be
 * able to communicate with the program (sending in input i.e. triggering
 * actions, invoking operations, and receiving program output i.e. being shown
 * the state or progress of an operation, seeing a queried value be displayed,
 * etc).
 * </p>
 * <p>
 * These are the fundamentals of a GUI. Every <i>instance</i> of a specific user
 * interface will share the same input-output features. To illustrate this
 * interpretation, consider a <b>log-in GUI</b> which is designed to allow a
 * user to log in to an application. Whether the background is an image of an
 * Aurora and there are text boxes stacked on top of one another for the user to
 * enter information, or the GUI requires that a user enter the username and
 * then the password in different tabs, the information that the user conveys to
 * the program (username, password, and trigger-log-in) as well as the
 * information that the program conveys back to the user (log-in-successful,
 * log-in-failed, syntactically-invalid-username, etc.) is all the same. Both
 * the Aurora-themed log-in window and the one that has the username and
 * password prompts on different tabs are <i>instances</i> of the log-in window
 * user interface.
 * </p>
 * <p>
 * The actual GUI design, layout, etc. which describe a concrete instance of a
 * specific user interface are handled by a <i>theme</i> which is tasked with
 * <i>presenting</i> the interface to the user. Themes are stored in the
 * <code>themes</code> package and implement the Presentation Java
 * <code>interface</code>s in this package to provide support for frontend's
 * scenes.
 * </p>
 * 
 * @author Palanath
 *
 */
package pala.apps.arlith.frontend.interfaces;