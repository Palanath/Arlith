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
import pala.apps.arlith.backend.common.protocol.types.IntegerValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.MessageValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class RetrieveMessagesRequest extends SimpleCommunicationProtocolRequest<ListValue<MessageValue>> {

	public GIDValue getThread() {
		return thread;
	}

	public void setThread(GIDValue thread) {
		this.thread = thread;
	}

	public IntegerValue getCount() {
		return count;
	}

	public void setCount(IntegerValue count) {
		this.count = count;
	}

	public static final String REQUEST_NAME = "retrieve-messages";
	private final static String THREAD_KEY = "thread", COUNT_KEY = "count";

	private GIDValue thread;
	private IntegerValue count;

	public RetrieveMessagesRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
		count = new IntegerValue(properties.get(COUNT_KEY));
	}

	public RetrieveMessagesRequest(GIDValue thread, IntegerValue count) {
		super(REQUEST_NAME);
		this.thread = thread;
		this.count = count;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());
		object.put(COUNT_KEY, count.json());
	}

	@Override
	protected ListValue<MessageValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, MessageValue::new);
	}

	@Override
	public ListValue<MessageValue> receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, ObjectNotFoundError, AccessDeniedError {
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
