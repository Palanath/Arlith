package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class CreateCommmunityInviteRequest extends SimpleCommunicationProtocolRequest<TextValue> {
	public static final String REQUEST_NAME = "create-commmunity-invite";

	private static final String COMMUNITY_KEY = "community";

	private GIDValue community;

	public CreateCommmunityInviteRequest(GIDValue community) {
		super(REQUEST_NAME);
		this.community = community;
	}

	public CreateCommmunityInviteRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		community = new GIDValue(properties.get(COMMUNITY_KEY));
	}

	public GIDValue getCommunity() {
		return community;
	}

	public CreateCommmunityInviteRequest setCommunity(GIDValue community) {
		this.community = community;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(COMMUNITY_KEY, community.json());
	}

	@Override
	public TextValue parseReturnValue(JSONValue json) {
		return new TextValue(json);
	}

	@Override
	public TextValue receiveResponse(CommunicationConnection client)
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