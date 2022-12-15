package pala.apps.arlith.api.communication.protocol.requests;

import pala.apps.arlith.api.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.api.communication.protocol.errors.AccessDeniedError;
import pala.apps.arlith.api.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.api.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.api.communication.protocol.errors.RateLimitError;
import pala.apps.arlith.api.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.api.communication.protocol.errors.ServerError;
import pala.apps.arlith.api.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.api.communication.protocol.types.GIDValue;
import pala.apps.arlith.api.communication.protocol.types.ListValue;
import pala.apps.arlith.api.communication.protocol.types.UserValue;
import pala.apps.arlith.api.connections.scp.CommunicationConnection;
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
