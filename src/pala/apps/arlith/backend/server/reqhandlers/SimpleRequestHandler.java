package pala.apps.arlith.backend.server.reqhandlers;

import java.util.function.Function;

import pala.apps.arlith.backend.common.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestConnection;
import pala.apps.arlith.backend.server.contracts.serversystems.RequestHandler;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONObject;

public abstract class SimpleRequestHandler<R extends CommunicationProtocolRequest<?>> implements RequestHandler {

	public interface SRHFunc<F, T> {
		T perform(F from) throws ClassCastException, UnknownCommStateException, BlockException;

		static <F, T> SRHFunc<F, T> from(Function<? super F, ? extends T> func) {
			return func::apply;
		}
	}

	private final SRHFunc<? super JSONObject, ? extends R> reifier;

	public SimpleRequestHandler(SRHFunc<? super JSONObject, ? extends R> reifier) {
		this.reifier = reifier;
	}

	public R reify(JSONObject req) throws ClassCastException, UnknownCommStateException, BlockException {
		return reifier.perform(req);
	}

	protected abstract void handle(R r, RequestConnection client)
			throws UnknownCommStateException, BlockException, ClassCastException;

	public final void handle(JSONObject request, RequestConnection client)
			throws ClassCastException, UnknownCommStateException, BlockException {
		handle(reify(request), client);
	}
}
