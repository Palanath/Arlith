package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.communication.protocol.types.CompletionValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ChangeEmailRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public static final String REQUEST_NAME = "change-email";
	private static final String EMAIL_KEY = "email";

	private TextValue email;

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	public ChangeEmailRequest(String email) {
		super(REQUEST_NAME);
		this.email = new TextValue(email);
	}

	public ChangeEmailRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		email = new TextValue(properties.get(EMAIL_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(EMAIL_KEY, email.json());
	}

	public TextValue getEmail() {
		return email;
	}

	public String email() {
		return email.getValue();
	}

	public void setEmail(TextValue email) {
		this.email = email;
	}

}
