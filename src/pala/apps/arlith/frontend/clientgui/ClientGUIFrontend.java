package pala.apps.arlith.frontend.clientgui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pala.apps.arlith.backend.client.ArlithClientBuilder;
import pala.apps.arlith.frontend.ArlithFrontend;
import pala.apps.arlith.frontend.clientgui.logic.login.LogInLogicImpl;
import pala.apps.arlith.frontend.clientgui.themes.arlithdefault.ArlithDefaultTheme;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInLogic;
import pala.apps.arlith.frontend.clientgui.uispec.login.LogInPresentation;
import pala.apps.arlith.libraries.frontends.Frontend;
import pala.apps.arlith.libraries.frontends.FrontendScene;
import pala.libs.generic.guis.Window.WindowLoadFailureException;

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
 * is active (such occurs through an invocation of {@link #show(Logic)}).
 * </p>
 * 
 * @author Palanath
 *
 */
public class ClientGUIFrontend implements Frontend {

	private final Stage stage;
	private final List<Theme> themes = new ArrayList<>();
	{
		// Add default theme.
		themes.add(new ArlithDefaultTheme());
	}

	public ClientGUIFrontend(Stage stage) {
		this.stage = stage;
	}

	/**
	 * <p>
	 * Shows the scene represented by the specified {@link Logic}. This method
	 * queries the available {@link #getThemes() themes}, in order, for a
	 * {@link Presentation} object that pairs with the specified {@link Logic}
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
	public <P extends Presentation<L>, L extends Logic<P>> void show(L logic) {
		P p;
		for (Theme t : getThemes())
			if ((p = t.supply(logic)) != null)
				try {
					logic.hook(p);
					stage.setScene(p.getScene());
					return;
				} catch (WindowLoadFailureException e) {
					ArlithFrontend.getGuiLogger()
							.err("Failed to load a presentation for a scene; (Debug Info: LOGIC CLASS="
									+ logic.getClass() + ", PRESENTATION CLASS=" + p.getClass() + ')');
					ArlithFrontend.getGuiLogger().err(e);
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
			show(new LogInLogicImpl(this, new ArlithClientBuilder()));
		} catch (RuntimeException e) {
			ArlithFrontend.getGuiLogger().err("Failed to show the log in scene.");
			ArlithFrontend.getGuiLogger().err(e);
		}
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

}
