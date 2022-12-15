package pala.apps.arlith.app.server.contracts.serversystems;

import java.util.Collection;

import pala.apps.arlith.api.connections.networking.BlockException;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.apps.arlith.app.server.MalformedIncomingRequestException;
import pala.apps.arlith.app.server.RequestNotSupportedException;
import pala.libs.generic.json.JSONObject;

/**
 * <p>
 * A {@link RequestMapper} is a structure that stores {@link RequestHandler}s
 * and calls the appropriate handler when given a request. When called,
 * {@link #handleRequest(RequestConnection)} reads a new request from the
 * specified {@link RequestConnection} and determines which of the registered
 * {@link RequestHandler}s should handle it. It then calls that
 * {@link RequestHandler}, passing it the request and the
 * {@link RequestConnection}.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestMapper {

	/**
	 * <p>
	 * Gets the {@link Collection} of {@link RequestHandler}s tracked by this
	 * {@link RequestSystem}. This collection is backed by this
	 * {@link RequestSystem}; changes to the {@link RequestSystem}'s registered
	 * handlers will be reflected by the list.
	 * </p>
	 * <p>
	 * The {@link Collection} is not modifiable.
	 * </p>
	 * 
	 * @return The {@link Collection} of {@link RequestHandler}s tracked by this
	 *         {@link RequestSystem}.
	 */
	Collection<RequestHandler> getRequestHandlers();

//	/**
//	 * Adds the specified {@link RequestHandler} to this {@link RequestSystem}.
//	 * 
//	 * @param handler The {@link RequestHandler} to add.
//	 */
//	default void removeHandler(RequestHandler handler) {
//		getRequestHandlers().remove(handler);
//	}
//
//	default void addHandler(RequestHandler handler) {
//		getRequestHandlers().add(handler);
//	}

	/**
	 * Adds the specified {@link RequestHandler} to this {@link RequestMapper}.
	 * Repeated additions of handlers under the same request name overwrite older
	 * additions.
	 * 
	 * @param requestName The name of the request being handled. This should exactly
	 *                    match the name that the client communicates with.
	 * @param handler     The {@link RequestHandler}.
	 */
	void addHandler(String requestName, RequestHandler handler);

	/**
	 * Removes the {@link RequestHandler} with the specified name from this
	 * {@link RequestMapper}.
	 * 
	 * @param requestName The request's exact name. This should exactly match the
	 *                    name that the protocol communicates with.
	 */
	void removeHandler(String requestName);

	/**
	 * <p>
	 * Attempts to handle a request sent to this server over the specified
	 * {@link RequestConnection}.
	 * </p>
	 * 
	 * @param connection The {@link RequestConnection} to read from and invoke the
	 *                   request for.
	 * @throws BlockException                    If a {@link BlockException} occurs.
	 * @throws UnknownCommStateException         If the state of the connection is
	 *                                           no longer known (and the connection
	 *                                           should be terminated).
	 * @throws MalformedIncomingRequestException In case the incoming request is
	 *                                           malformed.
	 * @throws ClassCastException                In case the incoming request is not
	 *                                           a valid {@link JSONObject}.
	 *                                           (requests are communicated via
	 *                                           {@link JSONObject}s. If an int is
	 *                                           sent, for example, this exception
	 *                                           is raised.)
	 * @throws RequestNotSupportedException      If the request being invoked by the
	 *                                           client is not supported by this
	 *                                           {@link RequestSystem}. This means
	 *                                           that there is no handler in this
	 *                                           {@link RequestSystem} to handle the
	 *                                           type of request that was invoked.
	 */
	void handleRequest(RequestConnection connection) throws UnknownCommStateException, BlockException,
			ClassCastException, MalformedIncomingRequestException, RequestNotSupportedException;

}