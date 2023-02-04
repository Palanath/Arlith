package pala.apps.arlith.frontend.clientgui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.logic.login.LogInScene;
import pala.apps.arlith.frontend.clientgui.themes.arlithdefault.ArlithDefaultTheme;
import pala.apps.arlith.frontend.clientgui.themes.gray.GrayTheme;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.apps.arlith.libraries.frontends.Frontend;
import pala.apps.arlith.libraries.frontends.FrontendScene;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.util.Box;

/**
 * <h1>Client GUI Frontend</h1>
 * <p>
 * The {@link ClientGUIFrontend} class is the cardinal class for the Arlith's
 * JavaFX GUI Frontend used to operate the client. The class does follow the
 * general Frontend Framework (in that all of its API is split amongst this
 * class and the remaining classes in this package, used to represent scenes),
 * but does not make use of the {@link FrontendScene} type; each scene in the
 * Client GUI Frontend is distributed amongst multiple classes.
 * </p>
 * <h2>Structure</h2>
 * <p>
 * Each scene is <i>specified</i> by a {@link Logic} sub-interface and a
 * {@link Presentation} sub-interface (for example, the Log In scene has the
 * {@link LogInLogic} and {@link LogInPresentation} interfaces).
 * </p>
 * <p>
 * The {@link Logic} sub-interface specifies the <i>triggers</i> that the user
 * can invoke on the scene. (It is the responsibility of the presentation
 * implementation to invoke these trigger methods whenever the user attempts to
 * invoke one of them by interacting with the UI presented to them.) The
 * {@link Presentation} sub-interface specifies the (1) <i>inputs</i> and (2)
 * <i>outputs</i> that the user can (1) give to the program, for processing
 * (e.g. user inputs username to log in) and (2) receive from the program (e.g.
 * program tells user "10th invalid password attempt; account deleted").
 * </p>
 * <p>
 * The Client GUI gives one implementation of the {@link Logic} for each scene,
 * and a {@link Theme} is expected to give one implementation of a the
 * {@link Presentation} for each scene (that it wishes to support). These are
 * implementations of the <i>sub-interfaces</i> for <i>the</i> scene in
 * question.
 * </p>
 * <h3>Forgoing of {@link FrontendScene}</h3>
 * <p>
 * The {@link FrontendScene} class was forgone in creating this
 * structure/framework for displaying {@link Scene}s because adding a scene
 * class does not seem to provide very much value in regard to ease of clarity
 * in the the model of the framework. Additionally, scene classes would need to
 * be made for every GUI, but would likely provide redundant/unnecessary
 * features that can be handled by this class (e.g. showing the scene). They may
 * be added later.
 * </p>
 * <h2>Showing Scenes</h2>
 * <p>
 * Each {@link Presentation} implementation provides a {@link Scene} object
 * which gets shown to the active {@link Stage} when that {@link Presentation}
 * is active (such occurs through an invocation of
 * {@link #loadPresentation(Logic)}).
 * </p>
 * 
 * @author Palanath
 *
 */
public class ClientGUIFrontend implements Frontend {

	private static final Object STAGE_UI_KEY = new Object();

	/**
	 * <p>
	 * Represents a {@link UserInterface}. {@link UserInterface}s generally act as a
	 * controller and the "entry point" for the graphical UI that they show. To show
	 * a {@link UserInterface}, its {@link #display()} or {@link #display(Stage)}
	 * methods can be called.
	 * </p>
	 * <h2>Lifecycle</h2>
	 * <h3>Construction &amp; Configuration</h3>
	 * <p>
	 * Immediately after construction, a {@link UserInterface} is considered not in
	 * use. From construction and until it is {@link #display(Stage) displayed}, a
	 * {@link UserInterface} can be configured through configuration methods that it
	 * may choose to expose to calling code. For configurable
	 * {@link UserInterface}s, this may do things like alter the behavior it
	 * exhibits once it is shown.
	 * </p>
	 * <h3>Display</h3>
	 * <p>
	 * After the preliminary configuration, calling code invokes
	 * {@link #display(Stage)} (or {@link #display()}), which then invokes
	 * {@link #show(Stage)}. During the showing process, the {@link UserInterface}
	 * is tasked with constructing (usually by calling
	 * {@link ClientGUIFrontend#loadPresentation(Logic)}) and configuring a
	 * {@link Presentation}. This includes hooking in to that {@link Presentation}
	 * (e.g. caching it as a field so that code running while the UI is shown has
	 * access to it).
	 * </p>
	 * <p>
	 * Most often, the {@link UserInterface} simply implements its own, specific
	 * {@link Logic} interface and acts as its own Logic/controller. This entails
	 * that, e.g., the {@link UserInterface} calls
	 * {@link ClientGUIFrontend#loadPresentation(Logic)} with itself as an argument
	 * (since it implements its own {@link Logic}).
	 * </p>
	 * <p>
	 * At this point, the {@link UserInterface} will have been configured and will
	 * also have its own {@link Presentation} that has just been constructed, but
	 * not yet shown. Much like the {@link UserInterface}, the {@link Presentation}
	 * can be configured at this point, by the {@link UserInterface}, before it is
	 * shown. Once it is ready to be shown, the {@link UserInterface} can call its
	 * {@link Presentation#show(Stage)} method, passing the same {@link Stage} given
	 * to the {@link UserInterface}. The {@link Presentation} should be shown before
	 * the {@link UserInterface}'s {@link #show(Stage)} method completes, so that
	 * the given {@link Stage} actually receives graphical elements to show to the
	 * user as a result of the call to {@link #show(Stage)}.
	 * </p>
	 * <h3>Operation</h3>
	 * <p>
	 * Once the {@link #show(Stage)} method completes, the {@link UserInterface}
	 * (and {@link Presentation}) will be in operation. Code in the
	 * {@link UserInterface} will primarily run whenever the user sends controller
	 * input (e.g. pushes a button and {@link UserInterface} logic needs to execute
	 * as a result) or when other events invoke it (e.g. an event handler attached
	 * to the {@link ArlithClient} receives an event and the {@link UserInterface}
	 * logic code is called to handle it). This operation will continue as normal
	 * until some code inside the {@link UserInterface} instantiates a
	 * <i>different</i> {@link UserInterface} (instance) and calls <i>its</i>
	 * {@link #display(Stage)} method. This will begin the disposal of this
	 * {@link UserInterface} (and then begin the showing of the new
	 * {@link UserInterface}). This most commonly happens when the user clicks a
	 * button or some controller input causing the {@link UserInterface} to need to
	 * display another {@link UserInterface}.
	 * </p>
	 * <p>
	 * To show another {@link UserInterface}, this {@link UserInterface} need only
	 * instantiate that other {@link UserInterface} and call its
	 * {@link #show(Stage)} method. This {@link UserInterface} should not call its
	 * own {@link #dispose()} method; that will be done by the {@link UserInterface}
	 * class framework.
	 * </p>
	 * <h3>Disposal</h3>
	 * <p>
	 * Before another {@link UserInterface}'s {@link #show(Stage)} method is called,
	 * this framework calls the current {@link UserInterface}'s {@link #dispose()}
	 * method, to allow the currently shown {@link UserInterface} to dispose of its
	 * {@link Presentation}, unhook any event handlers, free any file locks or other
	 * resources, and do any other cleanup it needs before it is hidden. The
	 * {@link UserInterface} should call {@link Presentation#dispose()} on its
	 * {@link Presentation} during the runtime of its own {@link #dispose()} method
	 * so that the {@link Presentation} is appropriately cleaned. <b>After the
	 * {@link #dispose()} method completes, the {@link UserInterface} automatically
	 * clears the {@link Stage} of any {@link Scene} that was already in it before
	 * the new {@link UserInterface}'s {@link #show(Stage)} method is called.</b>
	 * </p>
	 * <h2>Error Handling</h2>
	 * <p>
	 * Construction of a new {@link UserInterface}, showing it, constructing a new
	 * {@link Presentation}, showing <i>it</i>, and disposal of
	 * {@link UserInterface}s and {@link Presentation}s can all result in errors.
	 * </p>
	 * <p>
	 * If at any time during the invocation of {@link #display()} or
	 * {@link #display(Stage)} an error is encountered, the error is propagated to
	 * the caller as a {@link WindowLoadFailureException}. This includes during
	 * disposal of the previous window.
	 * </p>
	 * <p>
	 * In regard to {@link UserInterface}s only being displayable once, if disposal
	 * of the previous window was the cause of the error, then the
	 * {@link UserInterface} that was attempted to be shown will <i>not</i> have
	 * been shown and will still be applicable to be displayed. In essence, failure
	 * to clear the old {@link UserInterface} from a {@link Stage} as part of an
	 * attempt to show a new {@link UserInterface} will not cause that
	 * {@link UserInterface} to have been considered shown, nor will it cause the
	 * {@link UserInterface}'s {@link #show(Stage)} method to run.
	 * </p>
	 * <h2>FX Thread</h2>
	 * <p>
	 * The {@link #display(Stage)} and {@link #display()} methods automatically
	 * invoke {@link #show(Stage)} on the JavaFX Platform thread, if not already
	 * called on it, so that their modification of the {@link Stage}, its UI
	 * components, or any other JavaFX objects does not cause exceptions. <b>This is
	 * not done for disposal of the previous {@link UserInterface}</b>, so
	 * invocations of {@link #dispose()} might be called on a non-JFX thread. If
	 * such needs to be avoided, the {@link #dispose()} methods, themselves, should
	 * account for this, particularly in the {@link Presentation#dispose()}
	 * implementations.
	 * </p>
	 * 
	 * @author Palanath
	 *
	 */
	public abstract class UserInterface {

		protected final ClientGUIFrontend getFrontend() {
			return ClientGUIFrontend.this;
		}

		/**
		 * Convenience method to load a {@link Presentation} using this
		 * {@link UserInterface} object as the {@link Logic}. <b>This method is designed
		 * to be called only when this {@link UserInterface} implements {@link Logic}
		 * and acts as its own {@link Logic} instance.</b> If this object does not
		 * implement the {@link Logic} interface, this method will result in an
		 * exception.
		 * 
		 * @param <L> The specific {@link Logic} subtype.
		 * @param <P> The specific {@link Presentation} subtype.
		 * @return The newly loaded {@link Presentation}.
		 */
		@SuppressWarnings("unchecked")
		protected final <L extends Logic<P>, P extends Presentation<L>> P loadPresentation() {
			return ClientGUIFrontend.this.loadPresentation((L) this);
		}

		/**
		 * <p>
		 * Shows this {@link UserInterface} on the specified {@link Stage}. This method
		 * is called when this {@link UserInterface} is being {@link #display(Stage)
		 * displayed} to allow the UI to show its graphical components on the provided
		 * {@link Stage}. This method is only called after the previous
		 * {@link UserInterface}, if any, has been cleaned out of the {@link Stage}.
		 * </p>
		 * <p>
		 * See {@link UserInterface} class documentation for more details.
		 * </p>
		 * <p>
		 * Note that this method is only ever invoked on the Java FX Application thread.
		 * </p>
		 * 
		 * @param stage The {@link Stage} that this {@link UserInterface} should show
		 *              itself on.
		 */
		protected abstract void show(Stage stage) throws WindowLoadFailureException;

		private boolean called;

		/**
		 * <p>
		 * Called when this {@link UserInterface} is being hidden from the user so that
		 * another {@link UserInterface} can be shown. This method should clean out any
		 * resources that the {@link UserInterface} held and free the {@link Stage} it
		 * is being rendered on so that the next {@link UserInterface} can use it.
		 * </p>
		 * <p>
		 * Information controlled and used entirely by this {@link UserInterface} which
		 * needs to "persist" between displays of this {@link UserInterface} can be
		 * stored in the {@link Stage} using {@link Stage#getProperties()}, but note
		 * that these data will persist only in the provided {@link Stage}, so if this
		 * {@link UserInterface} is shown on a different {@link Window} then the
		 * information will not be there.
		 * </p>
		 * <p>
		 * By default, this method does nothing.
		 * </p>
		 * <p>
		 * Note that this method is <b>not</b> called on the JavaFX Platform thread by
		 * default.
		 * </p>
		 */
		protected void dispose() throws WindowLoadFailureException {

		}

		/**
		 * Displays this {@link UserInterface} on the default {@link StackOverflowError}
		 * controlled by the {@link ClientGUIFrontend} that owns this
		 * {@link UserInterface}.
		 * 
		 * @throws WindowLoadFailureException If an error occurs while attempting to
		 *                                    display the stage.
		 */
		public final void display() throws WindowLoadFailureException {
			display(stage);
		}

		public final void display(Stage stage) throws WindowLoadFailureException {
			if (called)
				throw new RuntimeException(
						"Cannot show the same user interface instance twice (This is a developer bug).");
			if (stage.getProperties().containsKey(STAGE_UI_KEY))
				((UserInterface) stage.getProperties().get(STAGE_UI_KEY)).dispose();
			stage.getProperties().put(STAGE_UI_KEY, this);

			// If we're not on the FX thread, we have to invoke the show method on the FX
			// thread and then have this thread WAIT until that one completes, either
			// exceptionally or normally.
			if (!Platform.isFxApplicationThread()) {
				Box<Exception> ex = new Box<>();// Used for inter-thread communication.
				synchronized (ex) {
					Platform.runLater(() -> {
						synchronized (ex) {
							// This can't start executing until ex.wait() is called in the caller thread
							// below.
							try {
								show(stage);
							} catch (Exception e) {
								ex.value = e;
							} finally {
								ex.notify();// Wake the caller thread once we leave the synch(ex) block.
							}
						}
					});
					try {
						ex.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();// This should not get called.
					}
					// By now, the code will have run on the JFX thread.
					if (ex.value != null)
						throw ex.value instanceof WindowLoadFailureException ? (WindowLoadFailureException) ex.value
								: new WindowLoadFailureException(ex.value);
				}
			} else
				try {
					show(stage);
				} catch (WindowLoadFailureException e) {
					throw e;
				} catch (Exception e) {
					throw new WindowLoadFailureException(e);
				}

			called = true;
		}

	}

	private final Stage stage;
	private final List<Theme> themes = new ArrayList<>();
	{
		// Add test theme for testing.
		themes.add(new GrayTheme());
		// Add default theme.
		themes.add(new ArlithDefaultTheme());
	}
	private ArlithClient client;

	public ClientGUIFrontend(Stage stage) {
		this.stage = stage;
	}

	/**
	 * <p>
	 * Shows the scene represented by the specified {@link Logic}. This method may
	 * be called from any thread, and (if not called on the FX thread) may return
	 * before the new scene is shown.
	 * </p>
	 * <p>
	 * This method queries the available {@link #getThemes() themes}, in order, for
	 * a {@link Presentation} object that pairs with the specified {@link Logic}
	 * class. If one is found, it is displayed, the {@link Logic} is hooked to it
	 * (when it's instantiated), and it is hooked to the {@link Logic} (via
	 * {@link Logic#hook(Presentation)}).
	 * </p>
	 * <p>
	 * After that process completes, an attempt is made to show the
	 * {@link Presentation} by querying its {@link Presentation#getScene()} method
	 * and supplying the result to this {@link ClientGUIFrontend}'s {@link #stage}'s
	 * {@link Stage#setScene(Scene)} method. If that fails, the next {@link Theme},
	 * if any, is queried for a {@link Presentation} and the process repeats until
	 * all {@link Theme}s are exhausted. If all {@link Theme}s are exhausted and no
	 * suitable {@link Presentation} can be found and displayed, this method throws
	 * a {@link RuntimeException}.
	 * </p>
	 * 
	 * @param logic The {@link Logic} to show the scene of.
	 */
	public <P extends Presentation<L>, L extends Logic<P>> P loadPresentation(L logic) {
		for (Theme t : getThemes()) {
			P p;
			if ((p = t.supply(logic)) != null)
				return p;
		}
		throw new RuntimeException(
				"Couldn't load a presentation for the specified scene's logic class; there was no presentation available by any of the loaded themes. (Loaded themes: "
						+ themes + ')');
	}

	@Override
	public void launch() {
		// Prepare and show initial scene.
		Platform.setImplicitExit(true);
		stage.show();
		try {
			new LogInScene(this, new ArlithClientBuilder()).display();
		} catch (WindowLoadFailureException e) {
			ArlithFrontend.getGuiLogger().err("Failed to show the log in scene.");
			ArlithFrontend.getGuiLogger().err(e);
		}
		stage.centerOnScreen();
	}

	/**
	 * Gets the ordered, modifiable {@link List} of {@link Theme}s active for this
	 * application. Whenever instantiating a presentation for a GUI, the first
	 * {@link Theme} is queried. If it does not have a presentation for the GUI, the
	 * next is queried, and so on and so forth.
	 * 
	 * @return
	 */
	public List<Theme> getThemes() {
		return themes;
	}

	public Stage getStage() {
		return stage;
	}

	/**
	 * <p>
	 * Sets the active, logged-in client for use by UIs in the frontend. This is
	 * called by the {@link LogInLogic} implementation to set the client available
	 * to other UI components after logging in succeeds. Once a client has been
	 * built through logging in or creating an account, if it loses connection to
	 * the server, it will simply reestablish the connection; it does not need to be
	 * reconnected.
	 * </p>
	 * <p>
	 * The frontend and its UI components have been designed around a reliance on
	 * this property not changing once first set by the {@link LogInLogic}. As some
	 * UIs may register event handlers or otherwise be directly linked with the
	 * client, changing it mid-use can have widespread and weird effects.
	 * </p>
	 * 
	 * @param client The logged-in client.
	 */
	public void setClient(ArlithClient client) {
		this.client = client;
	}

	/**
	 * Returns the logged-in client for use by the UIs in this frontend.
	 * 
	 * @return
	 */
	public ArlithClient getClient() {
		return client;
	}

}
