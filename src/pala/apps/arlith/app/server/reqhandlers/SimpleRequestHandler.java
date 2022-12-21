package pala.apps.arlith.app.server.reqhandlers;

import java.util.function.Function;

import pala.apps.arlith.app.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.app.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.backend.communication.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.backend.connections.networking.BlockException;
import pala.apps.arlith.backend.connections.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;

public abstract class SimpleRequestHandler<R extends CommunicationProtocolRequest<?>> implements RequestHandler {

	private final Function<JSONObject, R> reifier;

	public SimpleRequestHandler(Function<JSONObject, R> reifier) {
		this.reifier = reifier;
	}

	public R reify(JSONObject req) {
		return reifier.apply(req);
	}

	protected abstract void handle(R r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException;

	public final void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException {
		handle(reify(request), client);
	}
}
