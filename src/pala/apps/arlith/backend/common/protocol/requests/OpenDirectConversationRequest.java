package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
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
	public GIDValue receiveResponse(Connection client) throws SyntaxError, RateLimitError, ServerError, RestrictedError,
			ObjectNotFoundError, AccessDeniedError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
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
