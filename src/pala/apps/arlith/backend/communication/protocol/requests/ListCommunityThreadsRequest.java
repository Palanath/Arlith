package pala.apps.arlith.backend.communication.protocol.requests;

import pala.apps.arlith.backend.communication.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.communication.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.communication.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.communication.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.communication.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.communication.protocol.types.GIDValue;
import pala.apps.arlith.backend.communication.protocol.types.ListValue;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class ListCommunityThreadsRequest extends SimpleCommunicationProtocolRequest<ListValue<GIDValue>> {
	public static final String REQUEST_NAME = "list-community-threads";

	private static final String COMMUNITY_KEY = "community";

	private GIDValue community;

	public ListCommunityThreadsRequest(GIDValue community) {
		super(REQUEST_NAME);
		this.community = community;
	}

	public ListCommunityThreadsRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
	}

	public GIDValue getCommunity() {
		return community;
	}

	public ListCommunityThreadsRequest setCommunity(GIDValue community) {
		this.community = community;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
	}

	@Override
	public ListValue<GIDValue> parseReturnValue(JSONValue json) {
		return new ListValue<>(json, GIDValue::new);
	}

	@Override
	public ListValue<GIDValue> receiveResponse(CommunicationConnection client)
			throws IllegalCommunicationProtocolException, SyntaxError, ObjectNotFoundError, RestrictedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | ObjectNotFoundError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}