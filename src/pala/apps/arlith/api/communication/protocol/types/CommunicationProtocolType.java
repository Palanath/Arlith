package pala.apps.arlith.api.communication.protocol.types;

import pala.apps.arlith.api.connections.networking.Connection;
import pala.apps.arlith.api.connections.networking.UnknownCommStateException;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONValue;

/**
 * <h1>CommunicationProtocolType</h1>
 * <p>
 * Represents a <i>value</i> that can be sent between a client and a server.
 * {@link CommunicationProtocolType}s are used as arguments and response values for requests.
 * </p>
 * <p>
 * {@link CommunicationProtocolType}s have API methods for both clients and servers. Some code is
 * shared between both, e.g. {@link #json()}, but other methods are specific to
 * one or the other.
 * </p>
 * <style> table, th, td { border: solid 1px currentcolor; border-collapse:
 * collapse; } th, td { padding: 0.3em; } </style>
 * <table>
 * <tr>
 * <th>Method</th>
 * <th>API</th>
 * <th>Description</th>
 * <th>Related Methods</th>
 * </tr>
 * <tr>
 * <td>{@link #json()}</td>
 * <td>Both</td>
 * <td>Used to encode the JSON part of an <b>argument</b> sent to the server,
 * and to encode the JSON part of a <b>response</b> sent back from the server.
 * See {@link #json()} for more details.</td>
 * <td>{@link #toJSON()} - Outputs a condensed JSON string for sending over a
 * network.</td>
 * </tr>
 * <tr>
 * <td>{@link #send(Connection)}</td>
 * <td>Server</td>
 * <td>Used by the server to send a {@link CommunicationProtocolType}
 * <span style="text-decoration: underline;">as a response</span> to a request.
 * Sends both the JSON data <b>as well as any auxiliary data</b>.</td>
 * <td></td>
 * </tr>
 * </table>
 * <h2>Client API</h2>
 * <p>
 * The client sends {@link CommunicationProtocolType}s to the server as arguments to requests.
 * Most {@link CommunicationProtocolType}s are entirely encoded in JSON, so their {@link #json()}
 * and {@link #toJSON()} methods are used by the client. Client requests are
 * expected to obtain a JSON encoding of {@link CommunicationProtocolType}s, with a call to
 * {@link #toJSON()}, and then to incorporate that into their JSON package which
 * gets sent to the server.
 * </p>
 * <p>
 * Every {@link CommunicationProtocolType} is expected to have <i>some</i> JSON data. This may be
 * something trivial, like <code>TRUE</code> or <code>FALSE</code> in JSON for
 * {@link CommunicationProtocolType}s which simply need to indicate presence.
 * </p>
 * <p>
 * When the client uses a {@link CommunicationProtocolType}s which needs to send auxiliary data to
 * the server, <span style="color: red";>the {@link CommunicationProtocolType} subclass is
 * expected to have a means for the client to obtain/send that
 * information</span> and <span style="color: red;">the client is expected to
 * send that information on its own</span>; this class does not provide any API
 * for {@link CommunicationProtocolType}s which need to send auxiliary data
 * <span style="text-decoration: underline;">when sent as an argument in a
 * request.</span><a href="#ref1"><sup>[1]</sup></a>
 * </p>
 * <h2>Server API</h2>
 * <p>
 * The primary method in the server API of this class is the
 * {@link #send(Connection)} method; it is used by the server to send an entire
 * {@link CommunicationProtocolType} back as a response to a request. This method will simply send
 * the {@link #toJSON() JSON} representation of this {@link CommunicationProtocolType} back by
 * default, so subclasses that need to send e.g. auxiliary data are expected to
 * override this and send the JSON data representing them, followed by any
 * auxiliary data.
 * </p>
 * <footer>
 * <p>
 * <sup id="ref1">[1]</sup>This {@link CommunicationProtocolType} interface does not provide any
 * API for a {@link CommunicationProtocolType} to send auxiliary data when it's being sent as an
 * argument for a request. The reason for this is that, when sent as an
 * argument, the class representing the <b>request</b>, itself, needs to be able
 * to retrieve all the JSON encodings of all the values it sends to the server
 * and then package those into its JSON Package, which gets sent before
 * <b>any</b> auxiliary data. Requests need to coordinate with the
 * {@link CommunicationProtocolType}s they send to send all JSON data first, and then send the
 * auxiliary data.
 * </p>
 * <p>
 * However, when a {@link CommunicationProtocolType} is sent to the client, as a response to a
 * request, no other values are going to be sent with it, so as a matter of
 * convenience, {@link CommunicationProtocolType}s are expected to define a
 * {@link #send(Connection)} method that a server's request handler class can
 * call which will send the <i>entire</i> {@link CommunicationProtocolType} back, (typically first
 * the JSON, and then any auxiliary data).
 * </p>
 * </footer>
 * 
 * @author Palanath
 *
 */
public interface CommunicationProtocolType {
	JSONValue json();

	/**
	 * Returns an encoded {@link String} holding the JSON data that represents this
	 * {@link CommunicationProtocolType}.
	 * 
	 * @return Concise JSON representation of this {@link CommunicationProtocolType}.
	 * @author Palanath
	 */
	default String toJSON() {
		return JSONValue.toStringShort(json());
	}

	/**
	 * <p>
	 * Sends this {@link CommunicationProtocolType}, and any of its auxiliary data, over the specified
	 * {@link Connection} <b>as a response to a request</b>. This method is intended
	 * to be used by the server to send a single {@link JSONValue} (and possibly
	 * auxiliary data) over the network for the client to read. This method should
	 * send data in the same fashion that the appropriate client constructor would
	 * read it in.
	 * </p>
	 * <p>
	 * By default, this method simply sends the JSON data associated with this
	 * {@link CommunicationProtocolType} over the connection (by a call to {@link #toJSON()}). This
	 * method should be overridden if the type needs to send any additional
	 * (auxiliary) data. Conventionally, auxiliary data is sent after
	 * JSON.<a href="#ref1"><sup>[1]</sup></a> Typical overrides of this method will
	 * invoke the super definition and then send auxiliary data (if necessary).
	 * </p>
	 * <footer><sup id="ref1">[1]</sup>This is a part of the communication
	 * protocol's request specification for {@link CommunicationProtocolType}s that are sent <i>as
	 * arguments</i> of a request, but is not strictly required for response
	 * values.</footer>
	 * 
	 * @param connection The connection to send the response over.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs while sending the response.
	 */
	default void send(Connection connection) throws UnknownCommStateException {
		connection.sendString(toJSON());
	}

	/**
	 * The <code>null</code> {@link CommunicationProtocolType}. This gets converted to
	 * {@link JSONConstant#NULL} by a call to {@link #json()}. It has no auxiliary
	 * data.
	 */
	final CommunicationProtocolType NULL = () -> JSONConstant.NULL;

}
