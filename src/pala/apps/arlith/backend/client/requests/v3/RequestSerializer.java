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
 * {@link RequestSerializer} is a lightweight request-API class designed to
 * organize inquiries made over a {@link CommunicationConnection} (or set of
 * {@link CommunicationConnection}s) and maintain stability of the connection(s)
 * (i.e., assure that no two threads interleave network calls/inquiries).
 * </p>
 * <p>
 * It is a requirement for the server's request connections that {@link Inquiry
 * Inquiries} be made one at a time; once an {@link Inquiry} has been sent over
 * a {@link CommunicationConnection}, its response should be received before
 * another {@link Inquiry} is sent. (The reason for this is that {@link Inquiry
 * Inquiries} are guaranteed exclusive control of a
 * {@link CommunicationConnection} from the beginning of their
 * {@link Inquiry#sendRequest(CommunicationConnection) sending} to the end of
 * their {@link Inquiry#receiveResponse(CommunicationConnection) receiving}.) A
 * {@link RequestSerializer} assures stability over its underlying connection by
 * organizing and synchronizing uses of it: Requests are organized into a
 * conceptual queue or sequence, so that inquiries (and other supplementary
 * supported actions) are made sequentially and are synchronized so that two
 * threads may not attempt to manipulate the {@link CommunicationConnection} at
 * the same time, or to interleave {@link Inquiry Inquiries}, etc.
 * </p>
 * <p>
 * {@link RequestSerializer} is the most basic Client Request API type, as it
 * does nothing but guarantee {@link Inquiry}-conforming use of
 * {@link CommunicationConnection}s. {@link Inquiry Inquiries} are sent on the
 * calling thread, so calls to {@link #inquire(Inquiry)} block until the
 * {@link Inquiry} has fully completed (or fully failed).
 * </p>
 * <p>
 * If a {@link RequestSerializer} does not implement the concept of
 * {@link #start() start} and {@link #stop() stop} states, the {@link #start()}
 * and {@link #stop()} methods may be implemented to do nothing, or throw a
 * {@link RuntimeException}, although the former is generally preferred.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface RequestSerializer {

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

	// Start and Stop methods may need to be updated or removed (or new methods may
	// need to be added) as appropriate.
	/**
	 * Starts this {@link RequestSerializer}, if such operation is supported. This
	 * method puts the {@link RequestSerializer} into a state of operation so that
	 * calls to {@link #inquire(Inquiry)} work. This method most often enables an
	 * underlying {@link CommunicationConnection} over which {@link Inquiry
	 * inquiries} are then sent, though it does not necessarily have to (e.g., in a
	 * {@link RequestSerializer} implementation that wraps an already-working
	 * {@link CommunicationConnection}). Implementations that do support starting
	 * should do nothing upon a call to {@link #start()} that is made while the
	 * {@link RequestSerializer} is already in the started state.
	 */
	void start();

	/**
	 * Stops this {@link RequestSerializer}, if such operation is supported, so that
	 * the underlying {@link CommunicationConnection} (and other related resources)
	 * are released. Whether this {@link RequestSerializer} may be started again,
	 * after being stopped, is implementation-defined. Implementations that do
	 * support stopping should do nothing upon a call to {@link #stop()} that is
	 * made while the {@link RequestSerializer} is already in the stopped state.
	 */
	void stop();
}
