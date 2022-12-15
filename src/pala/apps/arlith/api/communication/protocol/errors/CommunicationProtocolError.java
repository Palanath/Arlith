package pala.apps.arlith.api.communication.protocol.errors;

import pala.apps.arlith.api.communication.protocol.types.CommunicationProtocolType;
import pala.apps.arlith.api.communication.protocol.types.LoginProblemValue;
import pala.apps.arlith.api.communication.protocol.types.TextValue;
import pala.libs.generic.JavaTools;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * Represents a Communication Protocol Error that is returned from the server.
 * It signifies that the normal completion of a request did not take place, and
 * provides data and reasoning, when possible, as to why that is the case.
 * </p>
 * <p>
 * Subclasses are expected to provide at least two constructors and be
 * immutable. Formally speaking, subclasses are expected to satisfy the
 * following two constructor requirements.
 * <ol>
 * <li>Subclasses should provide some way to create instances of the subclasses
 * based off of the logical components that make up the instance. For example,
 * the one unique property possessed by a {@link LoginError} is the
 * {@link LoginProblemValue} that represents what type of failure took place.
 * The {@link LoginError} class provides a constructor that takes such a value
 * to construct the {@link LoginError}. The first constructor should take in all
 * the required properties to produce a valid instance of the subclass of
 * {@link CommunicationProtocolError}.
 * <ul>
 * <li>Some errors may have particular and unusual forms, such as a type that
 * always contains exactly one of two different types of values (an error that
 * may include either a <code>phone-number</code> or <code>email</code>, but
 * never none or both). In these cases, the formal requirement may need to be
 * satisfied with multiple constructors, or a constructor that takes in some
 * kind of special, intermediary object. To build off of the phone number/email
 * example, such a class would provide one constructor that takes a
 * <code>PhoneNumber</code>, and another that takes an <code>Email</code>.</li>
 * </ul>
 * </li>
 * <li>Subclasses should provide a constructor that can be supplied a
 * {@link JSONObject}. The constructor should check the {@link JSONObject}'s
 * values to assure that the {@link JSONObject} represents a valid instance of
 * the {@link CommunicationProtocolError} subclass, then provide that
 * {@link JSONObject} to this class via a <code>super(...)</code> constructor
 * call. The {@link JSONObject} provided to these constructors is expected to
 * represent the type of {@link CommunicationProtocolError} being constructed
 * with it, and an {@link IllegalArgumentException} should be thrown if it is
 * found that this is not the case. The {@link CommunicationProtocolError}
 * subclass's constructor should then construct the object based off of the
 * validated properties in the {@link JSONObject}.
 * <ul>
 * <li>These constructors should not be aggressive so as to assert that the
 * provided {@link JSONObject} perfectly represents the
 * {@link CommunicationProtocolError} subclass object being constructed. This
 * means that any extraneous properties should be disregarded when constructing
 * the {@link CommunicationProtocolError} subclass object. An
 * {@link IllegalArgumentException} should be thrown if necessary properties are
 * missing from the {@link JSONObject}, or if the types of values in the object
 * do not match with the required type(s), or if the values that are required
 * are otherwise not valid for the {@link CommunicationProtocolError}
 * subclass.</li>
 * </ul>
 * </li>
 * </ol>
 * Subclasses' constructors should behave such that any created instance (that
 * is not tampered with after construction through unexpected means) will
 * <b>always</b> return values successfully during conversion via the getters
 * that the class provides.
 * </p>
 * <p>
 * Subclasses should, additionally, expose the properties that
 * characteristically define the classes' instances, via getters. They should
 * <b>not</b> expose setters that allow users to change those values, as error
 * instances are immutable.
 * </p>
 * 
 * @author Palanath
 *
 */
public abstract class CommunicationProtocolError extends Exception implements CommunicationProtocolType {

	public static final String ERROR_TYPE_KEY = "error", ERROR_MESSAGE_KEY = "error-message";

	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;

	private final TextValue errorType, errorMessage;

	/**
	 * <p>
	 * This constructor is for subclasses to use for their constructor that creates
	 * an instance of the subclass provided a {@link JSONObject} that represents it
	 * </p>
	 * <p>
	 * Constructs a {@link CommunicationProtocolError} provided the specified
	 * specific {@link CommunicationProtocolError} type, and the object to construct
	 * it from. The {@link JSONObject} is checked to see if it is actually of the
	 * {@link CommunicationProtocolError} type specified (by <code>type</code>). If
	 * the {@link JSONObject} does represent the type of error specified by the
	 * first argument to this constructor, then the constructor completes.
	 * Otherwise, an {@link IllegalArgumentException} is thrown.
	 * </p>
	 * 
	 * @param type  The type of {@link CommunicationProtocolError} that is to be
	 *              constructed from the provided {@link JSONObject}.
	 * @param error The {@link JSONObject} representing a
	 *              {@link CommunicationProtocolError}.
	 * @author Palanath
	 */
	protected CommunicationProtocolError(String type, JSONObject error) {
		super(error.getJString(ERROR_MESSAGE_KEY) == null ? null : error.getString(ERROR_MESSAGE_KEY));
		errorType = new TextValue(error.get(ERROR_TYPE_KEY));
		if (!type.equals(errorType.getValue()))
			throw new IllegalArgumentException(
					"Provided JSON Object is not this type of communication protocol error.");
		errorMessage = error.containsKey(ERROR_MESSAGE_KEY) ? new TextValue(error.get(ERROR_MESSAGE_KEY)) : null;
	}

	public CommunicationProtocolError(String type, String message) {
		this(new TextValue(type), message == null ? null : new TextValue(message));
	}

	public CommunicationProtocolError(TextValue type, TextValue message) {
		super(message == null ? null : message.getValue());
		JavaTools.requireNonNull(errorType = type);
		errorMessage = message;
	}

	public CommunicationProtocolError(String type) {
		this(type, (String) null);
	}

	public TextValue getErrorMessage() {
		return errorMessage;
	}

	public TextValue getErrorType() {
		return errorType;
	}

	public String getErrorTypeString() {
		return getErrorType().getValue();
	}

	public boolean hasErrorMessage() {
		return errorMessage != null;
	}

	public String getErrorMessageString() {
		return hasErrorMessage() ? getErrorMessage().getValue() : null;
	}

	public final JSONObject json() {
		JSONObject object = new JSONObject();
		object.put(ERROR_TYPE_KEY, errorType.json());
		if (hasErrorMessage())
			object.put(ERROR_MESSAGE_KEY, errorMessage.json());
		build(object);
		return object;
	}

	/**
	 * <p>
	 * Populates the specified {@link JSONObject} so that it contains all the data
	 * representing this {@link CommunicationProtocolError}. Instances of this
	 * specific {@link CommunicationProtocolError} type can be reconstructed from
	 * the returned {@link JSONObject} through the class's constructor that accepts
	 * a {@link JSONObject} argument.
	 * </p>
	 * <p>
	 * Sub-subclass implementations of this method should call their superclass
	 * counterparts so that superclasses can build their portion of data into the
	 * provided {@link JSONObject}.
	 * </p>
	 * 
	 * @param object The {@link JSONObject}.
	 * @author Palanath
	 */
	protected abstract void build(JSONObject object);

}
