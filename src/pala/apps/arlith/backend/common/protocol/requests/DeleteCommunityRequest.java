package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class DeleteCommunityRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {
	public static final String REQUEST_NAME = "delete-community";

	private static final String COMMUNITY_KEY = "community";

	private GIDValue community;

	public DeleteCommunityRequest(GIDValue community) {
		super(REQUEST_NAME);
		this.community = community;
	}

	public DeleteCommunityRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
	}

	public GIDValue getCommunity() {
		return community;
	}

	public DeleteCommunityRequest setCommunity(GIDValue community) {
		this.community = community;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
	}

	@Override
	public CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	@Override
	public CompletionValue receiveResponse(Connection client)
			throws IllegalCommunicationProtocolException, SyntaxError, ObjectNotFoundError, RestrictedError,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | ObjectNotFoundError | RestrictedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}
}