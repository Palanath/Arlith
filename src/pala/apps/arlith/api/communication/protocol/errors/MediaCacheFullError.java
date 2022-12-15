package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.types.LongValue;
import pala.libs.generic.json.JSONObject;

public class MediaCacheFullError extends CommunicationProtocolError {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "media-cache-full";
	private static final String MAX_SIZE_KEY = "max-size";

	private LongValue maxSize;

	public MediaCacheFullError(JSONObject error) {
		super(ERROR_TYPE, error);
		maxSize = new LongValue(error.get(MAX_SIZE_KEY));
	}

	public MediaCacheFullError(String message, LongValue maxSize) {
		super(ERROR_TYPE, message);
		this.maxSize = maxSize;
	}

	public MediaCacheFullError(LongValue maxSize) {
		super(ERROR_TYPE);
		this.maxSize = maxSize;
	}

	public LongValue getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(LongValue maxSize) {
		this.maxSize = maxSize;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(MAX_SIZE_KEY, maxSize.json());
	}

}
