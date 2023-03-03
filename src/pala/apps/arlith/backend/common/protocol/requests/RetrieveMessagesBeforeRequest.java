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
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
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
	public ListValue<MessageValue> receiveResponse(Connection client) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, ObjectNotFoundError, AccessDeniedError, IllegalCommunicationProtocolException,
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
