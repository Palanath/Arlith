package pala.apps.arlith.launchers.jfxclient;

import javafx.application.Application;
import javafx.stage.Stage;
import pala.apps.arlith.frontend.guis.ArlithFrontend;
import pala.apps.arlith.frontend.guis.login.LogInWindow;
import pala.libs.generic.guis.Window;

/**
 * <p>
 * A class that implements JavaFX's {@link Application} class to allow Arlith to
 * be started as a JavaFX GUI application.
 * </p>
 * <p>
 * Typical uses of this class are simply
 * {@link JFXApplicationClass#launch(Class, String...)} invoked with (1)
 * {@link JFXApplicationClass#class} and (2) the arguments provided to the
 * program when invoked, as arguments to the method.
 * </p>
 * <p>
 * This class is invoked whenever Arlith is launched in JavaFX GUI mode (through
 * {@link JFXLauncher}).
 * </p>
 * 
 * @author Palanath
 *
 */
public class JFXApplicationClass extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Window.getDefaultApplicationProperties().themeStylesheet
				.put("/pala/apps/arlith/graphics/stylesheets/default-styles.css");
		ArlithFrontend.prepareStage(primaryStage);
		primaryStage.show();
		new LogInWindow().display(primaryStage);
	}

}
