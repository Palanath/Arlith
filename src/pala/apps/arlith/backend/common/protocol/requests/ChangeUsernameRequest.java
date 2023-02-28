package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.CreateAccountError;
import pala.apps.arlith.backend.common.protocol.errors.EntityInUseError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ChangeUsernameRequest extends SimpleCommunicationProtocolRequest<TextValue> {
	public static final String REQUEST_NAME = "change-username";

	private static final String NAME_KEY = "name";

	private TextValue name;

	public ChangeUsernameRequest(TextValue name) {
		super(REQUEST_NAME);
		this.name = name;
	}

	public ChangeUsernameRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		name = new TextValue(properties.get(NAME_KEY));
	}

	public TextValue getName() {
		return name;
	}

	public void setName(TextValue name) {
		this.name = name;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(NAME_KEY, name.json());
	}

	@Override
	public TextValue parseReturnValue(JSONValue json) {
		return new TextValue(json);
	}

	@Override
	public TextValue receiveResponse(Connection client)
			throws RestrictedError, SyntaxError, RateLimitError, ServerError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException, CreateAccountError {
		try {
			return super.receiveResponse(client);
		} catch (RestrictedError | SyntaxError | RateLimitError | ServerError | CreateAccountError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}