package pala.apps.arlith.backend.common.protocol.errors;

import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.libs.generic.json.JSONObject;

public class EntityInUseError extends CommunicationProtocolError {
	/**
	 * SUID
	 */
	private static final long serialVersionUID = 1L;

	public static final String ERROR_TYPE = "entity-in-use", NAME_KEY = "name";

	private TextValue name;

	public EntityInUseError(JSONObject error) {
		super(ERROR_TYPE, error);
		name = new TextValue(error.get(NAME_KEY));
	}

	public EntityInUseError(String message, TextValue name) {
		super(ERROR_TYPE, message);
		this.name = name;
	}

	public EntityInUseError(TextValue name) {
		super(ERROR_TYPE);
		this.name = name;
	}

	public TextValue getName() {
		return name;
	}

	public String name() {
		return name.getValue();
	}

	public void setName(TextValue name) {
		this.name = name;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(NAME_KEY, name.json());
	}

}
