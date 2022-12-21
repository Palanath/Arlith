package pala.apps.arlith.backend.communication.protocol.events;

import java.time.Instant;

import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.libs.generic.events.EventType;
import pala.libs.generic.json.JSONObject;

public class ThreadAccessGainedEvent extends CommunicationProtocolEvent {

	public static final String EVENT_NAME = "thread-access-gained";
	private static final String THREAD_KEY = "thread";
	public static final EventType<ThreadAccessGainedEvent> THREAD_ACCESS_GAINED_EVENT = new EventType<>(COMMUNICATION_PROTOCOL_EVENT);

	private final GIDValue thread;

	@Override
	protected void build(JSONObject object) {
		object.put(THREAD_KEY, thread.json());
	}

	public ThreadAccessGainedEvent(Instant timestamp, GIDValue thread) {
		super(EVENT_NAME, timestamp);
		this.thread = thread;
	}

	public ThreadAccessGainedEvent(GIDValue thread) {
		super(EVENT_NAME);
		this.thread = thread;
	}

	public ThreadAccessGainedEvent(JSONObject properties) {
		super(EVENT_NAME, properties);
		thread = new GIDValue(properties.get(THREAD_KEY));
	}

	public GIDValue getThread() {
		return thread;
	}

}
