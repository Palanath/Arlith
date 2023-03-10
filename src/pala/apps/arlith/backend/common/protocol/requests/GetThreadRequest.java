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
import pala.apps.arlith.backend.common.protocol.types.ThreadValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
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
	public ThreadValue receiveResponse(Connection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, ObjectNotFoundError, AccessDeniedError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
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
