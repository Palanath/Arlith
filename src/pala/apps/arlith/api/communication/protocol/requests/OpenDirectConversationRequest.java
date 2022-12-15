package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class OpenDirectConversationRequest extends SimpleCommunicationProtocolRequest<GIDValue> {
	public GIDValue getRecipient() {
		return recipient;
	}

	public void setRecipient(GIDValue recipient) {
		this.recipient = recipient;
	}

	public static final String REQUEST_NAME = "open-direct-conversation";
	private final static String RECIPIENT_KEY = "recipient";

	public OpenDirectConversationRequest(JSONObject json) {
		super(REQUEST_NAME, json);
		recipient = new GIDValue(json.get(RECIPIENT_KEY));
	}

	public OpenDirectConversationRequest(GIDValue recipient) {
		super(REQUEST_NAME);
		setRecipient(recipient);
	}

	private GIDValue recipient;

	@Override
	protected void build(JSONObject object) {
		object.put(RECIPIENT_KEY, recipient.json());
	}

	@Override
	protected GIDValue parseReturnValue(JSONValue json) {
		return new GIDValue(json);
	}

	@Override
	public GIDValue receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, ObjectNotFoundError, AccessDeniedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError
				| AccessDeniedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}
