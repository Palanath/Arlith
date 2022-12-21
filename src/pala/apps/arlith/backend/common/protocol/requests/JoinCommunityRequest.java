package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.types.CompletionValue;
import pala.apps.arlith.backend.common.protocol.types.TextValue;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class JoinCommunityRequest extends SimpleCommunicationProtocolRequest<CompletionValue> {
	public static final String REQUEST_NAME = "join-community";

	private static final String INVITE_KEY = "invite";

	private TextValue invite;

	public JoinCommunityRequest(TextValue invite) {
		super(REQUEST_NAME);
		this.invite = invite;
	}

	public JoinCommunityRequest(JSONObject properties) {
		super(REQUEST_NAME, properties);
		invite = new TextValue(properties.get(INVITE_KEY));
	}

	public TextValue getInvite() {
		return invite;
	}

	public JoinCommunityRequest setInvite(TextValue invite) {
		this.invite = invite;
		return this;
	}

	@Override
	protected void build(JSONObject object) {
		object.put(INVITE_KEY, invite.json());
	}

	@Override
	public CompletionValue parseReturnValue(JSONValue json) {
		return new CompletionValue(json);
	}

	@Override
	public CompletionValue receiveResponse(CommunicationConnection client)
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