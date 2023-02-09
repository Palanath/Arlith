package pala.apps.arlith.backend.client.requests;

import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.backend.client.requests.v3.RequestQueueBase;
import pala.apps.arlith.backend.common.protocol.IllegalCommunicationProtocolException;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;

/**
 * <p>
 * Represents arbitrary communication to and from a server. Inquiries are the
 * most basic object in the {@link ArlithRuntime} Request API that represent
 * specific communications. {@link Inquiry} implementations control the entirety
 * of what is sent over the connection when the {@link Inquiry}'s send method is
 * invoked, and they also control the mechanism of how a response should be
 * interpreted or read from the connection. There is no structure to their
 * communication behavior beyond the fact that data is sent <i>and then</i>
 * received. (So, for example, an {@link Inquiry} instance may send multiple,
 * arbitrarily large blocks of data over the connection, and then read three
 * strings that the server sent back. Upon being called a second time, it may
 * send a single number, and then read two blocks of arbitrary byte data. Of
 * course, the server would need to be <i>responding</i> during the exchange
 * with all of the data the {@link Inquiry} implementation reads, but this
 * interface puts no restrictions on how the inquiring should take place.)
 * </p>
 * <p>
 * The {@link Inquiry} interface is distinct from the
 * {@link CommunicationProtocolRequest} type because {@link Inquiry} abstracts
 * any discrete communication between the client and the server; this type only
 * imposes the rule that an {@link Inquiry} is considered to occupy a network
 * connection after it {@link #sendRequest(Connection) sends} until after it
 * completes {@link #receiveResponse(Connection) receiving a response}. During
 * the time between the beginning of an inquiry's sending and the end of its
 * reception, no other data should be sent over the network, nor should any
 * attempts be made to read from it. Essentially, {@link Inquiry Inquiries} can
 * be used to send and receive any arbitrary data to and from the server in a
 * <i>send-receive</i> fashion; first an {@link Inquiry} is sent and then that
 * {@link Inquiry} is able to read the following data coming in from the server
 * before any other {@link Inquiry} is sent.
 * </p>
 * <p>
 * {@link Inquiry Inquiries} communicate using message blocks, as such API
 * provided by the {@link Connection} type.
 * </p>
 * 
 * @author Palanath
 *
 * @param <R> The type of the request's response.
 */
public interface Inquiry<R> {
	/**
	 * <p>
	 * Sends a single inquiry over the specified connection. The corresponding read
	 * is not performed unless {@link #receiveResponse(Connection)} is called.
	 * </p>
	 * 
	 * @param client The {@link Connection} to run the request on.
	 * @author Palanath
	 * @throws UnknownCommStateException If sending the request over the provided
	 *                                   {@link Connection} results in an
	 *                                   {@link UnknownCommStateException}.
	 */
	void sendRequest(Connection client) throws UnknownCommStateException;

	/**
	 * Decodes the response that the server sent into the return type of this
	 * {@link Inquiry} and returns it.
	 * 
	 * @param client The {@link Connection} to read the response from.
	 * @return The decoded response.
	 * @throws CommunicationProtocolError             In case a protocol error
	 *                                                occurs (e.g., the server sends
	 *                                                an invalid datum or the wrong
	 *                                                type of object, etc).
	 * @throws IllegalCommunicationProtocolException  In case the server sends back
	 *                                                a
	 *                                                {@link CommunicationProtocolError}
	 *                                                that is not supposed to be
	 *                                                sent back for this
	 *                                                {@link Inquiry} (usually this
	 *                                                indicates a version mismatch
	 *                                                between this client and the
	 *                                                server, where one or the other
	 *                                                has mismatching specifications
	 *                                                as to what type of errors can
	 *                                                be sent back in response to
	 *                                                this {@link Inquiry}, or there
	 *                                                could be a bug bug).
	 * @throws BlockException                         If reading the response from
	 *                                                the provided
	 *                                                {@link Connection} results in
	 *                                                a {@link BlockException}.
	 * @throws UnknownCommStateException              If reading the response from
	 *                                                the provided
	 *                                                {@link Connection} results in
	 *                                                an
	 *                                                {@link UnknownCommStateException}.
	 * @throws CommunicationProtocolConstructionError
	 */
	R receiveResponse(Connection client) throws CommunicationProtocolError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException;

	/**
	 * <p>
	 * Sends this {@link Inquiry} and then receives the response and returns it.
	 * This method simply calls {@link #sendRequest(Connection)} and then calls
	 * {@link #receiveResponse(Connection)} and returns the value. Any
	 * {@link CommunicationProtocolError}s are propagated.
	 * </p>
	 * <p>
	 * This method generally should not be overridden, as many facilities (e.g.
	 * {@link RequestQueueBase}s) rely on it to perform exactly as a convenience
	 * method that calls {@link #sendRequest(Connection)} then calls and returns
	 * {@link #receiveResponse(Connection)}.
	 * </p>
	 * 
	 * @param client The {@link Connection} over which to send and receive the
	 *               {@link Inquiry}.
	 * @return The result of the {@link Inquiry} as received from the server and
	 *         decoded.
	 * @throws CommunicationProtocolError             As thrown by
	 *                                                {@link #receiveResponse(Connection)}.
	 * @throws IllegalCommunicationProtocolException  As thrown by
	 *                                                {@link #receiveResponse(Connection)}.
	 * @throws BlockException                         If calling
	 *                                                {@link #receiveResponse(Connection)}
	 *                                                (reading the response from the
	 *                                                provided {@link Connection})
	 *                                                results in a
	 *                                                {@link BlockException}.
	 * @throws UnknownCommStateException              If either calling
	 *                                                {@link #receiveResponse(Connection)}
	 *                                                (reading the response from the
	 *                                                provided {@link Connection})
	 *                                                or calling
	 *                                                {@link #sendRequest(Connection)}
	 *                                                (sending the inquiry over the
	 *                                                provided {@link Connection})
	 *                                                results in an
	 *                                                {@link UnknownCommStateException}.
	 * @throws CommunicationProtocolConstructionError If reading the JSON response
	 *                                                from the {@link Connection}
	 *                                                succeeds but attempting to
	 *                                                reconstruct that JSON into a
	 *                                                valid Communication Protocol
	 *                                                object fails. This can be due
	 *                                                to the response being
	 *                                                malformed (e.g. the JSON does
	 *                                                not represent a valid
	 *                                                Communication Protocol Object
	 *                                                representation).
	 */
	default R inquire(Connection client) throws CommunicationProtocolError, IllegalCommunicationProtocolException,
			CommunicationProtocolConstructionError, UnknownCommStateException, BlockException {
		sendRequest(client);
		return receiveResponse(client);
	}

}
