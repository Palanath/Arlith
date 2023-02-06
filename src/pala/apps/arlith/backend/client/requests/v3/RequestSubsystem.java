package pala.apps.arlith.backend.client.requests.v3;

import pala.apps.arlith.backend.client.requests.Inquiry;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.AlreadyInCommunityError;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.errors.InvalidMediaError;
import pala.apps.arlith.backend.common.protocol.requests.ChangeEmailRequest;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

/**
 * <p>
 * The subsystem underlying Arlith's client that handles all requests the client
 * makes to the server. A {@link RequestSubsystem} manages a
 * {@link CommunicationConnection}, (or possibly even a set of
 * {@link CommunicationConnection}s), to the server, through which
 * {@link Inquiry Inquiries} are sent and responses are received.
 * </p>
 * <p>
 * {@link RequestSubsystem}s are used to manage and organize the sending of
 * requests over the connection(s) (assuring that no two <i>send</i> calls are
 * made in a row over the same connection, without an interposed <i>receive</i>
 * call; synchronizing code executing on the connection, etc.), to facilitate
 * sending requests and receiving responses by the client without having to
 * worry about organization and synchronization.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestSubsystem {

	/**
	 * <p>
	 * Makes an {@link Inquiry} to the server by calling
	 * {@link Inquiry#inquire(CommunicationConnection)}, and returns the response.
	 * The inquiry is synchronized against other uses of the underlying connection,
	 * so this method may be called from any thread at any time.
	 * </p>
	 * <p>
	 * The {@link Inquiry} is performed on the calling thread and this method blocks
	 * until the {@link Inquiry} finishes, either successfully or erroneously. If an
	 * error occurs, be it a {@link CommunicationProtocolError} or a runtime
	 * exception/error, it is propagated to the caller.
	 * </p>
	 * <p>
	 * Note that the only {@link CommunicationProtocolError}s that can be thrown by
	 * this method are those thrown by the call to
	 * {@link Inquiry#inquire(CommunicationConnection)} on the provided
	 * {@link Inquiry}; calling code may assume that handled
	 * {@link CommunicationProtocolError}s are only of such types. For example, if a
	 * {@link ChangeEmailRequest} is made, only those
	 * {@link CommunicationProtocolError}s declared as throwable from a call to
	 * {@link ChangeEmailRequest#receiveResponse(CommunicationConnection)} can be
	 * thrown from this method, so callers need not handle e.g.
	 * {@link InvalidMediaError}s or {@link AlreadyInCommunityError}s. Additionally,
	 * callers need not assume any {@link CommunicationProtocolError} can be thrown
	 * and then handle {@link CommunicationProtocolError}s generally as a result.
	 * </p>
	 * <p>
	 * Note that the {@link Inquiry#inquire(CommunicationConnection)} invocation
	 * performed by this method can also throw
	 * {@link IllegalCommunicationProtocolException}s, which are a subtype of
	 * {@link RuntimeException}, not {@link CommunicationProtocolError}.
	 * </p>
	 * 
	 * @param <R>     The type of result that the {@link Inquiry} receives as a
	 *                response.
	 * @param inquiry The {@link Inquiry} to run. The {@link Inquiry} is executed on
	 *                this thread and synchronized as appropriate so that the
	 *                requirement that <i>every send be immediately followed by an
	 *                accompanying, corresponding receive</i> not be violated.
	 * @return The result of the {@link Inquiry} received from the server.
	 * @throws CommunicationProtocolError If the reception of the inquiry results in
	 *                                    a {@link CommunicationProtocolError}. This
	 *                                    method will only throw
	 *                                    {@link CommunicationProtocolError}s thrown
	 *                                    by the call to
	 *                                    {@link Inquiry#inquire(CommunicationConnection)}
	 *                                    that this method makes.
	 */
	<R> R inquire(Inquiry<? extends R> inquiry) throws CommunicationProtocolError;

	void start();

	void stop();
}
