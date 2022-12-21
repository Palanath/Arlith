package pala.apps.arlith.backend.client.requests;

import pala.apps.arlith.application.ArlithRuntime;
import pala.apps.arlith.backend.common.protocol.errors.CommunicationProtocolError;
import pala.apps.arlith.backend.common.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.backend.connections.scp.CommunicationConnection;

/**
 * <p>
 * Represents arbitrary communication to and from a Application server. Inquiries
 * are the most basic object in the {@link ArlithRuntime} Request API that represent
 * communication. {@link Inquiry} implementations control the entirety of what
 * is sent over the connection when the {@link Inquiry}'s send method is
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
 * {@link Inquiry Inquiries} differ from {@link CommunicationProtocolRequest}s because this
 * {@link Inquiry} type abstracts any discrete communication between the client
 * and the server. The only rule is that an {@link Inquiry} is considered to
 * occupy a network connection after it {@link #sendRequest(CommunicationConnection)
 * sends} until after it completes {@link #receiveResponse(CommunicationConnection)
 * receiving a response}. During the time between the beginning of an inquiry's
 * sending and the end of its reception, no other data should be sent over the
 * network, nor should any attempts be made to read from it. Essentially,
 * {@link Inquiry Inquiries} can be used to send and receive any arbitrary data
 * to and from the server in a <i>send-receive</i> fashion; first an
 * {@link Inquiry} is sent and then that {@link Inquiry} is able to read the
 * following data coming in from the server before any other {@link Inquiry} is
 * sent.
 * </p>
 * 
 * @author Palanath
 *
 * @param <R> The type of the request's response.
 */
public interface Inquiry<R> {
	/**
	 * <p>
	 * Sends a single inquiry over the specified connection.
	 * </p>
	 * 
	 * @param client The {@link CommunicationConnection} to run the request on.
	 * @author Palanath
	 */
	void sendRequest(CommunicationConnection client);

	/**
	 * Decodes the response that the server sent over into the return type of this
	 * {@link Inquiry} and returns it.
	 * 
	 * @param client The {@link CommunicationConnection} to read the response from.
	 * @return The decoded response.
	 * @throws CommunicationProtocolError In case a protocol error occurs (e.g., the server sends an
	 *                  invalid datum or the wrong type of object, etc).
	 */
	R receiveResponse(CommunicationConnection client) throws CommunicationProtocolError;

	/**
	 * Sends this {@link Inquiry} and then receives the response and returns it.
	 * This method simply calls {@link #sendRequest(CommunicationConnection)} and then
	 * calls {@link #receiveResponse(CommunicationConnection)} and returns the value.
	 * Any {@link CommunicationProtocolError}s are propagated.
	 * 
	 * @param client
	 * @return
	 * @throws CommunicationProtocolError
	 */
	default R inquire(CommunicationConnection client) throws CommunicationProtocolError {
		sendRequest(client);
		return receiveResponse(client);
	}

}
