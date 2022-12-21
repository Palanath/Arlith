package pala.apps.arlith.backend.common.protocol.errors;

import pala.apps.arlith.backend.common.protocol.types.BooleanValue;
import pala.apps.arlith.backend.common.protocol.types.LongValue;
import pala.libs.generic.json.JSONObject;

public class RateLimitError extends CommunicationProtocolError {

	public static final String ERROR_TYPE = "rate-limit";
	private static final String BREAKING_KEY = "breaking", SLEEP_TIME_KEY = "sleep-time";

	private final BooleanValue breaking;
	private final LongValue sleepTime;

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	public RateLimitError(JSONObject json) {
		super(ERROR_TYPE, json);
		sleepTime = new LongValue(json.get(SLEEP_TIME_KEY));
		breaking = json.containsKey(BREAKING_KEY) ? new BooleanValue(json.get(BREAKING_KEY)) : BooleanValue.FALSE;
	}

	public RateLimitError(long sleepTime) {
		this(sleepTime, null);
	}

	public RateLimitError(long sleepTime, Boolean breaking) {
		this(null, sleepTime, breaking);
	}

	public RateLimitError(String message, long sleepTime) {
		this(message, sleepTime, null);
	}

	public RateLimitError(String message, long sleepTime, Boolean breaking) {
		super(ERROR_TYPE, message);
		this.breaking = breaking == null ? BooleanValue.FALSE : new BooleanValue(breaking);
		this.sleepTime = new LongValue(sleepTime);
	}

	public BooleanValue getBreaking() {
		return breaking;
	}

	public LongValue getSleepTime() {
		return sleepTime;
	}

	public boolean isBreaking() {
		return breaking.is();
	}

	public long getSleepTimeLong() {
		return sleepTime.getValue();
	}

	@Override
	protected void build(JSONObject object) {
		object.put(BREAKING_KEY, breaking.json());
		object.put(SLEEP_TIME_KEY, sleepTime.json());
	}

}
