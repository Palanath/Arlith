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
import pala.apps.arlith.backend.communication.protocol.types.IntegerValue;
import pala.apps.arlith.backend.communication.protocol.types.ListValue;
import pala.apps.arlith.backend.communication.protocol.types.MessageValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class RetrieveMessagesBeforeRequest extends SimpleCommunicationProtocolRequest<ListValue<MessageValue>> {

	public GIDValue getThread() {
		return thread;
	}

	public void setThread(GIDValue thread) {
		this.thread = thread;
	}

	public IntegerValue getCount() {
		return count;
	}

	public GIDValue getPivot() {
		return pivot;
	}

	public void setPivot(GIDValue pivot) {
		this.pivot = pivot;
	}

	public void setCount(IntegerValue count) {
		this.count = count;
	}

	public static final String REQUEST_NAME = "retrieve-messages-before";
	private final static String THREAD_KEY = "thread", COUNT_KEY = "count", PIVOT_KEY = "pivot";

	private GIDValue thread;
	private IntegerValue count;
	private GIDValue pivot;

	public RetrieveMessagesBeforeRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
		count = new IntegerValue(properties.get(COUNT_KEY));
		pivot = new GIDValue(properties.get(PIVOT_KEY));
	}

	public RetrieveMessagesBeforeRequest(GIDValue thread, IntegerValue count, GIDValue pivot) {
		super(REQUEST_NAME);
		JavaTools.requireNonNull(thread, count);
		this.thread = thread;
		this.count = count;
		this.pivot = pivot;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());
		object.put(COUNT_KEY, count.json());
		object.put(PIVOT_KEY, pivot.json());
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
