package pala.apps.arlith.frontend.server.contracts.serversystems;

import pala.apps.arlith.backend.communication.protocol.types.CommunicationProtocolType;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;

public interface RequestHandler {
	void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException;

	final CommunicationProtocolType NULL = CommunicationProtocolType.NULL;
}
