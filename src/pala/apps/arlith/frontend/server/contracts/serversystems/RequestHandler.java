package pala.apps.arlith.frontend.server.contracts.serversystems;

import pala.apps.arlith.backend.common.protocol.types.CommunicationProtocolType;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;

public interface RequestHandler {
	void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException;

	final CommunicationProtocolType NULL = CommunicationProtocolType.NULL;
}
