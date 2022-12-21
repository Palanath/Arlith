package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class SendMessageRequest extends SimpleCommunicationProtocolRequest<MessageValue> {

	public GIDValue getThread() {
		return thread;
	}

	public void setThread(GIDValue thread) {
		this.thread = thread;
	}

	public TextValue getContent() {
		return content;
	}

	public void setContent(TextValue content) {
		this.content = content;
	}

	public static final String REQUEST_NAME = "send-message";
	private final static String THREAD_KEY = "thread", CONTENT_KEY = "content";

	private GIDValue thread;
	private TextValue content;

	public SendMessageRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
		content = new TextValue(properties.get(CONTENT_KEY));
	}

	public SendMessageRequest(GIDValue thread, TextValue content) {
		super(REQUEST_NAME);
		setThread(thread);
		setContent(content);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());
		object.put(CONTENT_KEY, content.json());
	}

	@Override
	protected MessageValue parseReturnValue(JSONValue json) {
		return new MessageValue(json);
	}

	@Override
	public MessageValue receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError, AccessDeniedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | AccessDeniedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
