package pala.apps.arlith.frontend.clientgui.themes.gray.login;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CreateAccountController {
	private final SilverTextBox usernamePrompt = new SilverTextBox(false, true),
			emailPrompt = new SilverTextBox(false, true), phoneNumberPrompt = new SilverTextBox(false, false),
			passwordPrompt = new SilverTextBox(true, true);
	private final StyledHyperlink backToLogInHyperlink = new StyledHyperlink("Back To Log In");
	private final StackPane hyperlinkContainer = new StackPane(backToLogInHyperlink);
	private final VBox passwordContainer = new VBox(10, passwordPrompt, hyperlinkContainer);
	private final VBox container = new VBox(20, usernamePrompt, emailPrompt, phoneNumberPrompt, passwordContainer);
	{
		usernamePrompt.setPrefWidth(350);
		emailPrompt.setPrefWidth(350);
		phoneNumberPrompt.setPrefWidth(350);
		passwordPrompt.setPrefWidth(350);
		usernamePrompt.getPrompt().setText("Username");
		emailPrompt.getPrompt().setText("Email");
		phoneNumberPrompt.getPrompt().setText("Phone Number");
		passwordPrompt.getPrompt().setText("Password");
	}

	public SilverTextBox getUsernamePrompt() {
		return usernamePrompt;
	}

	public SilverTextBox getEmailPrompt() {
		return emailPrompt;
	}

	public SilverTextBox getPhoneNumberPrompt() {
		return phoneNumberPrompt;
	}

	public SilverTextBox getPasswordPrompt() {
		return passwordPrompt;
	}

	public VBox getContainer() {
		return container;
	}

	public StyledHyperlink getBackToLogInHyperlink() {
		return backToLogInHyperlink;
	}
}
