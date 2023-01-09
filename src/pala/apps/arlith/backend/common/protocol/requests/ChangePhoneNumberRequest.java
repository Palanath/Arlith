package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
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

	/**
	 * <p>
	 * Creates a new {@link ChangePhoneNumberRequest} given the provided phone
	 * number. The provided phone number may be <code>null</code>, in which case the
	 * resulting {@link ChangePhoneNumberRequest} would represent a request to
	 * remove the registered phone number associated with the logged-in user.
	 * </p>
	 * <p>
	 * The phone number provided is encoded through a {@link TextValue} object. If
	 * <code>null</code> is provided, no {@link TextValue} object is created.
	 * </p>
	 * 
	 * @param phoneNumber The phone number, or <code>null</code>.
	 */
	public ChangePhoneNumberRequest(String phoneNumber) {
		this(phoneNumber == null ? null : new TextValue(phoneNumber));
	}

	/**
	 * <p>
	 * Creates a new {@link ChangePhoneNumberRequest} given the provided phone
	 * number. The provided phone number may be <code>null</code>, in which case the
	 * resulting {@link ChangePhoneNumberRequest} would represent a request to
	 * remove the registered phone number associated with the logged-in user.
	 * 
	 * @param phoneNumber
	 */
	public ChangePhoneNumberRequest(TextValue phoneNumber) {
		super(REQUEST_NAME);
		this.phoneNumber = phoneNumber;
	}

	public ChangePhoneNumberRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		if (properties.containsKey(PHONE_NUMBER_KEY))
			phoneNumber = new TextValue(properties.get(PHONE_NUMBER_KEY));
	}

	@Override
	protected void build(JSONObject object) {
		object.put(PHONE_NUMBER_KEY, phoneNumber.json());
	}

	public TextValue getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * Gets the {@link String} value of the phone number to be sent in this request,
	 * or <code>null</code> if this is a request to remove any registered phone
	 * number.
	 * 
	 * @return The phone number, as a string, or <code>null</code>.
	 */
	public String phoneNumber() {
		return phoneNumber.getValue();
	}

	/**
	 * Sets the phone number that this object is a request to change the user's
	 * associated phone number to. If the specified phone number is
	 * <code>null</code>, represent a request to remove the registered phone number
	 * associated with the logged-in user.
	 * 
	 * @param phoneNumber The phone number, or <code>null</code>.
	 */
	public void setPhoneNumber(TextValue phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
