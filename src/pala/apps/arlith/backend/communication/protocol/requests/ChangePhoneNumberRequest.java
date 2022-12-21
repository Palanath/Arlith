package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.communication.protocol.types.CompletionValue;
import pala.apps.arlith.backend.communication.protocol.types.TextValue;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ChangePhoneNumberRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {

	public static final String REQUEST_NAME = "change-phone-number";
	private static final String PHONE_NUMBER_KEY = "phone-number";

	private TextValue phoneNumber;

	@Override
	protected CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	public ChangePhoneNumberRequest(String email) {
		super(REQUEST_NAME);
		this.phoneNumber = new TextValue(email);
	}

	public ChangePhoneNumberRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		phoneNumber = new TextValue(properties.get(PHONE_NUMBER_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(PHONE_NUMBER_KEY, phoneNumber.json());
	}

	public TextValue getPhoneNumber() {
		return phoneNumber;
	}

	public String phoneNumber() {
		return phoneNumber.getValue();
	}

	public void setPhoneNumber(TextValue phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
