package pala.apps.arlith.frontend.clientgui.themes.gray.login;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * <p>
 * Controller for the log in portion of the Log In GUI. The log in portion
 * encompasses a {@link #getContainer()} {@link VBox} which contains the "Log
 * In" {@link Text} title and the input prompts for logging in.
 * </p>
 * <p>
 * The GUI components handled by this class are fully contained within
 * {@link #getContainer() the container}.
 * </p>
 * 
 * @author Palanath
 *
 */
public class LogInController {
	private final SilverTextBox logInIdentifierPrompt = new SilverTextBox(false, true),
			passwordPrompt = new SilverTextBox(true, true);
	private final StyledHyperlink createAccountHyperlink = new StyledHyperlink("Create Account...");
	private final StackPane hyperlinkContainer = new StackPane(createAccountHyperlink);
	private final VBox passwordContainer = new VBox(10, passwordPrompt, hyperlinkContainer);
	{
		passwordContainer.setFillWidth(true);
		hyperlinkContainer.setAlignment(Pos.CENTER_LEFT);
		logInIdentifierPrompt.getPrompt().setText("Account Tag/Email/Phone");
		logInIdentifierPrompt.setPrefWidth(300);
		passwordPrompt.getPrompt().setText("Password");
		passwordPrompt.setPrefWidth(300);
	}
	private final VBox container = new VBox(40, logInIdentifierPrompt, passwordContainer);

	public SilverTextBox getLogInIdentifierPrompt() {
		return logInIdentifierPrompt;
	}

	public SilverTextBox getPasswordPrompt() {
		return passwordPrompt;
	}

	public VBox getContainer() {
		return container;
	}

	public StyledHyperlink getCreateAccountHyperlink() {
		return createAccountHyperlink;
	}

}
