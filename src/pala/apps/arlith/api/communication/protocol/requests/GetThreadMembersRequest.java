package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.UserValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetThreadMembersRequest extends SimpleCommunicationProtocolRequest<ListValue<UserValue>> {

	private static final String REQUEST_NAME = "get-thread-members", THREAD_KEY = "thread";

	public GIDValue getThread() {
		return thread;
	}

	public void setThread(GIDValue thread) {
		this.thread = thread;
	}

	private GIDValue thread;

	public GetThreadMembersRequest(GIDValue thread) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME);
		this.thread = thread;
	}

	public GetThreadMembersRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());
	}

	@Override
	protected ListValue<UserValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, UserValue::new);
	}

	@Override
	public ListValue<UserValue> receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, ObjectNotFoundError, AccessDeniedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | AccessDeniedError
				| ObjectNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
