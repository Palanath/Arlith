package pala.apps.arlith.backend.common.protocol.requests;

import pala.apps.arlith.backend.common.gids.GID;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AccessDeniedError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.ObjectNotFoundError;
import pala.apps.arlith.backend.common.protocol.errors.RateLimitError;
import pala.apps.arlith.backend.common.protocol.errors.RestrictedError;
import pala.apps.arlith.backend.common.protocol.errors.ServerError;
import pala.apps.arlith.backend.common.protocol.errors.SyntaxError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.types.GIDValue;
import pala.apps.arlith.backend.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

public class GetCommunityOwnerRequest extends SimpleCommunicationProtocolRequest<GIDValue> {

	public static final String REQUEST_NAME = "get-community-owner";
	private final static String ID_KEY = "id";

	private GIDValue id;

	public GetCommunityOwnerRequest(JSONObject properties) throws CommunicationProtocolConstructionError {
		super(REQUEST_NAME, properties);
		id = new GIDValue(properties.get(ID_KEY));
	}

	public GetCommunityOwnerRequest(GIDValue id) {
		super(REQUEST_NAME);
		this.id = id;
	}

	public GIDValue getId() {
		return id;
	}

	public GID id() {
		return getId().getGid();
	}

	public void setId(GIDValue id) {
		this.id = id;
	}

	@Override
	public GIDValue receiveResponse(CommunicationConnection client) throws SyntaxError, RateLimitError, ServerError,
			RestrictedError, ObjectNotFoundError, AccessDeniedError {
		try {
			return super.receiveResponse(client);
		} catch (SyntaxError | RateLimitError | ServerError | RestrictedError | ObjectNotFoundError
				| AccessDeniedError e) {
			throw e;
		} catch (CommunicationProtocolError e) {
			throw new IllegalCommunicationProtocolException(e);
		}
	}

	@Override
	protected void build(JSONObject object) {
		object.put(ID_KEY, id.json());
	}

	@Override
	protected GIDValue parseReturnValue(JSONValue json) {
		return new GIDValue(json);
	}

}
