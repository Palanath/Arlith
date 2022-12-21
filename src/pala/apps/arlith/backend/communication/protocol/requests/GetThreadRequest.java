package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.errors.ServerError;
import pala.apps.arlith.backend.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.ThreadValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetThreadRequest extends SimpleCommunicationProtocolRequest<ThreadValue> {

	public static final String REQUEST_NAME = "get-thread";
	private final static String THREAD_KEY = "thread";

	private GIDValue thread;

	public void setThread(GIDValue thread) {
		this.thread = thread;
	}

	public GIDValue getThread() {
		return thread;
	}

	public GetThreadRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
	}

	public GetThreadRequest(GIDValue thread) {
		super(REQUEST_NAME);
		this.thread = thread;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());

	}

	@Override
	protected ThreadValue parseReturnValue(JSONValue json) {
		return new ThreadValue(json);
	}

	@Override
	public ThreadValue receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, ObjectNotFoundError, AccessDeniedError, IllegalCommunicationProtocolException {
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
