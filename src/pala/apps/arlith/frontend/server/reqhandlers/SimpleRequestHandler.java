package pala.apps.arlith.frontend.server.reqhandlers;

import java.util.function.Function;

import pala.apps.arlith.backend.common.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.frontend.server.contracts.serversystems.RequestHandler;
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
