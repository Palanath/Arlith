package pala.apps.arlith.frontend.guis.threadview;

import static pala.apps.arlith.frontend.guis.threadview.ThreadFormattingUtils.render;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pala.apps.arlith.application.JFXArlithRuntime;
import pala.apps.arlith.application.Logging;
import pala.apps.arlith.backend.client.ArlithClient;
import pala.apps.arlith.backend.client.api.ClientMessage;
import pala.apps.arlith.backend.client.api.ClientThread;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.events.MessageCreatedEvent;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.frontend.guis.BindHandlerPage;
import pala.apps.arlith.graphics.windows.ArlithWindow;
import pala.apps.arlith.libraries.graphics.dialogs.SimpleDialog;
import pala.apps.arlith.libraries.graphics.nodes.AdvancedColorPicker;
import pala.apps.arlith.libraries.graphics.nodes.StylePicker;
import pala.libs.generic.events.EventHandler;
import pala.libs.generic.generators.Generator;
import pala.libs.generic.guis.Window.WindowLoadFailureException;
import pala.libs.generic.javafx.FXTools;

/**
 * @author Palanath
 *
 */
public class ThreadViewPage extends BindHandlerPage {

	private static final Color TEXT_BOX_BACKGROUND_COLOR = Color.gray(0.05, 0.7);
	private static final int PFP_AND_TEXTBOX_SPACING = 12, PFP_SIZE = 48;
	private static final double TEXT_BOX_BUBBLINESS = 15, MESSAGE_GAP_PADDING = 2;
	static final double DEFAULT_FONT_SIZE = 16;

	private final ClientThread thread;
	private boolean reachedTop;
	private volatile boolean retrieving;
	private final Generator<ClientMessage> threadhist;
	private final ArlithClient client;
	private @FXML VBox output;
	private @FXML TextArea input;
	private @FXML ScrollPane scrollBox;

	private static final Object PFP_KEY = new Object(), MESSAGE_KEY = new Object();

	private Stage stage;

	public ThreadViewPage(ClientThread thread) {
		this.thread = thread;
		client = thread.client();
		threadhist = thread.history();
	}

	private EventHandler<? super MessageCreatedEvent> newMessageListener;
	private final javafx.event.EventHandler<? super KeyEvent> globalKeypressHandler = event -> {
		input.replaceSelection(event.getCharacter());
		input.requestFocus();
		event.consume();
	};

	@Override
	public void cleanup(ArlithWindow win) {
		super.cleanup(win);
		client.unregister(MessageCreatedEvent.MESSAGE_CREATED_EVENT, newMessageListener);
		stage.setMinHeight(0);
		stage.setMinWidth(0);
		stage.removeEventHandler(KeyEvent.KEY_TYPED, globalKeypressHandler);
	}

	private void reachedTop() {
		scrollBox.setOnScroll(null);
		reachedTop = true;
	}

	// TODO Optimize with bulk add ops.

	private boolean isHeader(int index) {
		return index == 0 || !fromSameUser(index, index - 1);
	}

	private boolean isSandwich(int index) {
		return index != 0 && index < output.getChildren().size() - 1 && fromSameUser(index, index - 1)
				&& fromSameUser(index, index + 1);
	}

	private boolean isFollower(int index) {
		if (index == output.getChildren().size() - 1)
			return !isHeader(index);
		else
			return !fromSameUser(index, index + 1);
	}

	private boolean fromSameUser(int first, int second) {
		return fromSameUser(output.getChildren().get(first), output.getChildren().get(second));
	}

	private void add(int index, ClientMessage msg) {
		final Node nod;
		if (index != 0) {
			ClientMessage before = getMessage(output.getChildren().get(index - 1));
			if (fromSameUser(before, msg)) {

				if (output.getChildren().size() > index
						&& fromSameUser(msg, getMessage(output.getChildren().get(index))))
					nod = renderSandwich(msg);
				else
					nod = renderFollower(msg);

				if (isFollower(index - 1))
					sandwichify(index - 1);

			} else {
				nod = renderHeader(msg);// The next node is possibly connected.

				if (isSandwich(index - 1)) {
					decap(index - 1);
					if (output.getChildren().size() > index)
						promote(index + 1);
				} else if (output.getChildren().size() > index && (isFollower(index + 1) || isSandwich(index + 1)))

					promote(index + 1);

			}
		} else {// Index is 0.
			nod = renderHeader(msg);// This is at the top (index=0) so it's going to be a header.

			if (!output.getChildren().isEmpty()) {// There was already at least one element being rendered.
				ClientMessage after = getMessage(output.getChildren().get(0));// Should currently have a head because it
																			// was already at the very top.

				// Now let's check if it should be grouped with the node we're adding...
				if (fromSameUser(msg, after)) {// ...If it should, it will be either a sandwich or tail.
					// Now we need to decide if it should be a sandwich or a follower. --> If it has
					// an element after it that is grouped with it, it needs to be a sandwich...
					if (output.getChildren().size() > 1 && fromSameUser(after, getMessage(output.getChildren().get(1))))
						sandwichify(0);
					else// ...otherwise, it needs to be a follower.
						decap(0);
				} else// ...Otherwise, it will be a header.
					promote(0);

			}
		}

		output.getChildren().add(index, nod);
	}

	private static boolean fromSameUser(Node first, Node second) {
		return fromSameUser(getMessage(first), getMessage(second));
	}

	private static boolean fromSameUser(ClientMessage first, ClientMessage second) {
		return first.getAuthor().id().equals(second.getAuthor().id());
	}

	private void decap(int index) {
		output.getChildren().set(index, renderFollower(getMessage(output.getChildren().get(index))));
	}

	private void sandwichify(int index) {
		output.getChildren().set(index, renderSandwich(getMessage(output.getChildren().get(index))));
	}

	private void promote(int index) {
		output.getChildren().set(index, renderHeader(getMessage(output.getChildren().get(index))));
	}

	private HBox renderHeader(ClientMessage msg) {

		Text name;
		try {
			name = new Text(msg.getAuthor().getName());
			bindable(name::setText).bind(msg.getAuthor().usernameView());
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to get the name of the author of a message.");
			Logging.err(e);
			Logging.err("Message content: " + msg.getText());
			throw new RuntimeException(e);
		}
		name.setFont(Font.font(24));
		name.setStyle("-fx-fill:a;");
		name.setFill(Color.WHITE);
		Node content = render(msg);
		VBox box = new VBox(name, content);
		box.setSpacing(15);
		ImageView pfp = new ImageView();
		pfp.setFitWidth(PFP_SIZE);
		pfp.setPreserveRatio(true);
		pfp.setSmooth(true);

		try {
			pfp.setImage(msg.getAuthor().getProfileIcon());
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to obtain the profile picture for the author of a message.");
			Logging.err(e);
			WritableImage img = new WritableImage(1, 1);
			img.getPixelWriter().setColor(0, 0, Color.FIREBRICK);
			pfp.setImage(img);
		}
		bindable(pfp::setImage).bind(msg.getAuthor().profileIconView());
		StackPane pfpbox = new StackPane(pfp);
		pfpbox.setPrefSize(PFP_SIZE, PFP_SIZE);
		HBox rootbox = new HBox(pfpbox, box);
		rootbox.setSpacing(PFP_AND_TEXTBOX_SPACING);
		box.setAlignment(Pos.CENTER_LEFT);

		rootbox.setAlignment(Pos.CENTER_LEFT);
		rootbox.setBackground(FXTools.getBackgroundFromColor(TEXT_BOX_BACKGROUND_COLOR, TEXT_BOX_BUBBLINESS));
		rootbox.getProperties().put(MESSAGE_KEY, msg);
		rootbox.getProperties().put(PFP_KEY, pfp);
		rootbox.setPadding(
				new Insets(MESSAGE_GAP_PADDING, TEXT_BOX_BUBBLINESS, MESSAGE_GAP_PADDING, PFP_AND_TEXTBOX_SPACING));
		VBox.setMargin(rootbox, new Insets(10, 5, 0, 5));
		return rootbox;
	}

	private StackPane renderSandwich(ClientMessage msg) {

		Node content = render(msg);
		StackPane rootbox = new StackPane(content);
		VBox.setMargin(rootbox,
				new Insets(0, PFP_SIZE + PFP_AND_TEXTBOX_SPACING + 5, 0, PFP_SIZE + PFP_AND_TEXTBOX_SPACING + 5));
		rootbox.setPadding(
				new Insets(MESSAGE_GAP_PADDING, TEXT_BOX_BUBBLINESS, MESSAGE_GAP_PADDING, TEXT_BOX_BUBBLINESS));

		rootbox.setAlignment(Pos.CENTER_LEFT);
		rootbox.setBackground(FXTools.getBackgroundFromColor(TEXT_BOX_BACKGROUND_COLOR));
		rootbox.getProperties().put(MESSAGE_KEY, msg);
		return rootbox;

	}

	private StackPane renderFollower(ClientMessage msg) {
		Node content = render(msg);
		StackPane rootbox = new StackPane(content);
		VBox.setMargin(rootbox,
				new Insets(0, PFP_SIZE + PFP_AND_TEXTBOX_SPACING + 5, 0, PFP_SIZE + PFP_AND_TEXTBOX_SPACING + 5));
		rootbox.setPadding(new Insets(MESSAGE_GAP_PADDING, TEXT_BOX_BUBBLINESS,
				TEXT_BOX_BUBBLINESS - MESSAGE_GAP_PADDING, TEXT_BOX_BUBBLINESS));

		rootbox.setAlignment(Pos.CENTER_LEFT);
		rootbox.setBackground(new Background(new BackgroundFill(TEXT_BOX_BACKGROUND_COLOR,
				new CornerRadii(0, 0, TEXT_BOX_BUBBLINESS, TEXT_BOX_BUBBLINESS, false), null)));
		rootbox.getProperties().put(MESSAGE_KEY, msg);
		return rootbox;
	}

	private void add(ClientMessage message) {
		add(output.getChildren().size(), message);
	}

	private static ClientMessage getMessage(Node rootbox) {
		return (ClientMessage) rootbox.getProperties().get(MESSAGE_KEY);
	}

	@Override
	public void show(ArlithWindow win) throws WindowLoadFailureException {
		stage = (Stage) win.getContentRoot().getScene().getWindow();
		stage.setMinWidth(600);
		stage.setMinHeight(500);

		stage.addEventHandler(KeyEvent.KEY_TYPED, globalKeypressHandler);

		FXMLLoader loader = new FXMLLoader();
		loader.setController(this);
		try {
			win.setContent(loader.load(ThreadViewPage.class.getResourceAsStream("ThreadViewGUI.fxml")));
			input.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				if (event.getCode() == KeyCode.ENTER) {
					if (event.isShiftDown())
						input.replaceSelection("\n");
					else
						send();
					event.consume();
				}
			});
			input.setWrapText(true);
		} catch (IOException e) {
			Logging.err("Failed to load the CENTER PANEL of the Thread Viewer window.");
			throw new WindowLoadFailureException(e);
		}

		EditMenu editMenu = new EditMenu(input);
		editMenu.setOnShowing(event -> editMenu.getPaste().setDisable(!Clipboard.getSystemClipboard().hasString()));

		StylePicker sp = new StylePicker();
		sp.getStylesheets().setAll(win.getRoot().getStylesheets());
		sp.getColorPicker().setColor(JFXArlithRuntime.getBaseColor());

		Circle circle = new Circle(5);
		circle.fillProperty().bind(sp.getColorPicker().colorProperty());

		SimpleDialog dialog = new SimpleDialog(stage, sp);
		dialog.setTitle("Formatting");

		MenuItem color = new MenuItem("Color", circle), style = new MenuItem("Style");
		color.setOnAction(event -> {
			dialog.show();
			sp.showColorPicker();
		});
		style.setOnAction(a -> {
			dialog.show();
			sp.showFontPicker();
		});

		Menu formattingMenu = new Menu("Formatting");

		formattingMenu.getItems().addAll(color, style);

		ContextMenu menu = new ContextMenu(editMenu, formattingMenu);
		input.setContextMenu(menu);

		sp.setFontSize(DEFAULT_FONT_SIZE);

		sp.setOnSelect(() -> {
			dialog.close();
			StringBuilder prep = new StringBuilder(), end = new StringBuilder();

			// If any of the following properties are different from their default value,
			// include styling code for them.
			if (!JFXArlithRuntime.getBaseColor().equals(sp.getColor())) {
				prep.append("<color v=\"").append(AdvancedColorPicker.format(sp.getColorPicker().getColor()))
						.append("\">");
				end.append("</>");
			}

			if (sp.isUnderlined()) {
				prep.append("<u>");
				end.insert(0, "</>");// Potentially suboptimal, but in case for future.
			}

			if (sp.isStrikethrough()) {
				prep.append("<st>");
				end.insert(0, "</>");
			}

			if (sp.getBold() != FontWeight.NORMAL) {
				prep.append("<b>");
				end.insert(0, "</>");
			}

			if (sp.getItalicized() != FontPosture.REGULAR) {
				prep.append("<i>");
				end.insert(0, "</>");
			}

			if (!Font.getDefault().getFamily().equals(sp.getFamily())) {
				prep.append("<f v=\"").append(sp.getFamily().replace("\\", "\\\\").replace("\"", "\\\"")).append("\">");
				end.insert(0, "</>");
			}

			// Change default font size AND change this; the default font size is 16, which
			// is a little less than the preferred default font size for message text.
			if (DEFAULT_FONT_SIZE != sp.getFontSize()) {
				prep.append("<s v=").append(sp.getFontSize()).append('>');
				end.insert(0, "</>");
			}

			IndexRange ir = input.getSelection();

			input.insertText(ir.getEnd(), end.toString());
			String sel = input.getText(ir.getStart(), ir.getEnd());
			if (!sel.isEmpty())
				input.replaceText(ir, sel.replace("\\", "\\\\").replace("<", "\\<"));
			input.insertText(ir.getStart(), prep.toString());
		});

		// Internal ScrollPane node consumes scroll events whenever the view is not at
		// the top of the scroll pane's content.

		// TODO Make not laggy
		scrollBox.setOnScroll(event -> {
			if (!reachedTop && event.getDeltaY() > 0 && !retrieving) {
				retrieving = true;
				try {
					List<ClientMessage> msgs = threadhist.collect(20);// threadhist climbs "up" the history, from latest
																	// message to very first message sent.
					if (msgs.size() < 20)
						reachedTop();
					for (ClientMessage m : msgs)
						add(0, m);// Add the latest message that is not being rendered, to the top of our GUI.
				} finally {
					retrieving = false;
				}
			}
		});

		scrollBox.vvalueProperty().addListener((InvalidationListener) observable -> {
			if (!reachedTop && scrollBox.getVvalue() == scrollBox.getVmin() && !retrieving) {
				retrieving = true;
				try {
					List<ClientMessage> msgs = threadhist.collect(20);
					if (msgs.size() < 20)
						reachedTop();
					for (ClientMessage m : msgs)
						add(0, m);

				} finally {
					retrieving = false;
				}
			}
		});

		synchronized (thread) {
			final List<ClientMessage> messages;
			try {
				messages = threadhist.collect(50);
				if (messages.size() < 50)
					reachedTop();
			} catch (RuntimeException e) {
				Logging.err("An error occurred while loading messages from a thread.");
				Logging.err(e);
				throw new WindowLoadFailureException("Failed to request messages in thread from server.", e);
			}

			client.register(MessageCreatedEvent.MESSAGE_CREATED_EVENT, newMessageListener = event -> {
				boolean e = scrollBox.getVvalue() == scrollBox.getVmax();
				Platform.runLater(() -> {
					scrollBox.getParent().layout();
					add(event.getMessage());
					if (e)
						scrollBox.setVvalue(scrollBox.getVmax());
				});
			});
			for (ClientMessage msg : messages)
				add(0, msg);

			scrollBox.getParent().layout();
			scrollBox.setVvalue(scrollBox.getVmax());
		}
		try {
			win.getMenuBar().title.setText(thread.getName().toUpperCase());
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err("Failed to get thread's name.");
			Logging.err(e);
			win.getMenuBar().title.setText("A THREAD");
		}
		output.setSpacing(0);
	}

	private void add(MessageValue msg) {
		try {
			add(thread.getMessage(msg.id()));
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError
				| AccessDeniedError | RuntimeException e) {
			Logging.err("An unexpected error occurred while trying to show a message that you sent.");
			throw new RuntimeException(e);
		}
	}

	private @FXML void send() {
		String in = input.getText().trim();
		if (in.isEmpty())
			return;
		try {
			thread.sendMessageRequest(in).then(t -> {
				boolean e = scrollBox.getVvalue() == scrollBox.getVmax();
				Platform.runLater(() -> {
					add(t);
					scrollBox.getParent().layout();
					if (e)
						scrollBox.setVvalue(scrollBox.getVmax());
				});
			}).handle(t -> {
				Logging.err("Failed to send your message.");
				Logging.err(t);
			}).get();
		} catch (CommunicationProtocolError | RuntimeException e) {
			Logging.err(e);
		}
		input.clear();
	}

}
