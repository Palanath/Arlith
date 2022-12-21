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
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetCommunityUsersRequest extends SimpleCommunicationProtocolRequest<ListValue<UserValue>> {

	public static final String REQUEST_NAME = "get-community-users";
	private final static String COMMUNITY_KEY = "community";

	private GIDValue community;

	public GIDValue getCommunity() {
		return community;
	}

	public GetCommunityUsersRequest(GIDValue community) {
		super(REQUEST_NAME);
		setCommunity(community);
	}

	public GetCommunityUsersRequest(JSONObject json) {
		super(REQUEST_NAME, json);
		community = new GIDValue(json.get(COMMUNITY_KEY));
	}

	public void setCommunity(GIDValue community) {
		this.community = community;
	}

	@Override
	protected ListValue<UserValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, UserValue::new);
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
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
