package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.libs.generic.json.JSONObject;

public class ObjectNotFoundError extends CommunicationProtocolError {

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	public static final String ERROR_TYPE = "object-not-found";
	private static final String OBJECT_KEY = "object";

	public ObjectNotFoundError(JSONObject error) {
		super(ERROR_TYPE, error);
		object = error.containsKey(OBJECT_KEY) ? new GIDValue(error.get(OBJECT_KEY)) : null;
	}

	public ObjectNotFoundError(GIDValue gid, String message) {
		super(ERROR_TYPE, message);
		object = gid;
	}

	public ObjectNotFoundError(GIDValue gid) {
		super(ERROR_TYPE);
		object = gid;
	}

	public ObjectNotFoundError() {
		this((GIDValue) null);
	}

	public ObjectNotFoundError(String message) {
		this(null, message);
	}

	private final GIDValue object;

	public GIDValue getObject() {
		return object;
	}

	public boolean hasObject() {
		return object != null;
	}

	@Override
	protected void build(JSONObject object) {
		if (this.object != null)
			object.put(OBJECT_KEY, this.object.json());
	}

}
