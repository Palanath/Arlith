package pala.apps.arlith.api.communication.protocol.errors;

import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;

public final class CommunicationProtocolErrorDiscerner {

	public static CommunicationProtocolError determineError(JSONObject jjj) throws IllegalArgumentException {
		String errType = jjj.getString("error");

		switch (errType) {
		case AccessDeniedError.ERROR_TYPE:
			return new AccessDeniedError(jjj);
		case AuthError.ERROR_TYPE:
			return new AuthError(jjj);
		case CreateAccountError.ERROR_TYPE:
			return new CreateAccountError(jjj);
		case InvalidConnectionStateError.ERROR_TYPE:
			return new InvalidConnectionStateError(jjj);
		case LoginError.ERROR_TYPE:
			return new LoginError(jjj);
		case ObjectNotFoundError.ERROR_TYPE:
			return new ObjectNotFoundError(jjj);
		case RateLimitError.ERROR_TYPE:
			return new RateLimitError(jjj);
		case RestrictedError.ERROR_TYPE:
			return new RestrictedError(jjj);
		case ServerError.ERROR_TYPE:
			return new ServerError(jjj);
		case SyntaxError.ERROR_TYPE:
			return new SyntaxError(jjj);
		case TFAError.ERROR_TYPE:
			return new TFAError(jjj);
		case TFARequiredError.ERROR_TYPE:
			return new TFARequiredError(jjj);
		case MediaTooLargeError.ERROR_TYPE:
			return new MediaTooLargeError(jjj);
		case MediaCacheFullError.ERROR_TYPE:
			return new MediaCacheFullError(jjj);
		case MediaNotFoundError.ERROR_TYPE:
			return new MediaNotFoundError(jjj);
		case InvalidMediaError.ERROR_TYPE:
			return new InvalidMediaError(jjj);
		case EntityInUseError.ERROR_TYPE:
			return new EntityInUseError(jjj);

		// XXX Add errors here.
		default:
			throw new IllegalArgumentException("Unsupported error.");
		}
	}

	public static JSONValue checkErrors(JSONValue value) throws CommunicationProtocolError {
		if (value instanceof JSONObject) {
			JSONObject jsv = (JSONObject) value;
			if (jsv.get("error") instanceof JSONString)
				throw determineError(jsv);
		}
		return value;
	}
}
