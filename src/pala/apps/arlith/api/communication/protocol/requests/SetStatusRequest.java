package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.CompletionValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class SetStatusRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public static final String REQUEST_NAME = "set-status";
	private final static String STATUS_KEY = "status";
	private TextValue status;

	public TextValue getStatus() {
		return status;
	}

	public void setStatus(TextValue status) {
		this.status = status;
	}

	public SetStatusRequest(TextValue status) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.status = status;
	}

	public SetStatusRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		status = new TextValue(properties.get(STATUS_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(STATUS_KEY, status.json());
	}

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
			throws SyntaxError, RateLimitError, ServerError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
