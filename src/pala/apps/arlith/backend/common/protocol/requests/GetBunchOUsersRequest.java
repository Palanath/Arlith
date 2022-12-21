package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.ListValue;
import pala.apps.arlith.backend.common.protocol.types.UserValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetBunchOUsersRequest extends SimpleCommunicationProtocolRequest<ListValue<UserValue>> {

	public static final String REQUEST_NAME = "get-bunch-o-users";
	private final static String USERS_KEY = "users";

	private ListValue<GIDValue> users;

	public ListValue<GIDValue> getUsers() {
		return users;
	}

	public GetBunchOUsersRequest(ListValue<GIDValue> users) {
		super(REQUEST_NAME);
		setUsers(users);
	}

	public GetBunchOUsersRequest(JSONObject json) {
		super(REQUEST_NAME, json);
		users = new ListValue<GIDValue>(json.get(USERS_KEY), GIDValue::new);
	}

	public void setUsers(ListValue<GIDValue> users) {
		this.users = users;
	}

	@Override
	protected ListValue<UserValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, UserValue::new);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(USERS_KEY, users.json());
	}

	@Override
	public ListValue<UserValue> receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError,
			ServerError, RestrictedError, AccessDeniedError, ObjectNotFoundError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | AccessDeniedError
				| ObjectNotFoundError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

}
