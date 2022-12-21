package pala.apps.arlith.backend.common.protocol.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.apps.arlith.backend.common.protocol.requests.CommunicationProtocolRequest;
import pala.apps.arlith.backend.common.protocol.requests.SetProfileIconRequest;
import pala.apps.arlith.backend.streams.InputStream;
import pala.apps.arlith.libraries.networking.BlockException;
import pala.apps.arlith.libraries.networking.Connection;
import pala.apps.arlith.libraries.networking.UnknownCommStateException;
import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONNumber;
import pala.libs.generic.json.JSONObject;
import pala.libs.generic.json.JSONValue;

/**
 * Represents a single piece of media. This class has a {@link #media} property
 * 
 * @author Palanath
 *
 */
public class PieceOMediaValue implements CommunicationProtocolType {

	private byte[] media;

	public byte[] getMedia() {
		return media;
	}

	public void setMedia(byte[] media) {
		this.media = media;
	}

	public PieceOMediaValue(JSONValue json, Connection client) throws UnknownCommStateException, BlockException {
		if (!(json instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError(
					"Expected a JSONNumber (for PieceOMediaValue media size), found " + json, json);
		media = client.readBlockLong();
	}

	public PieceOMediaValue(JSONValue json, CommunicationConnection client) {
		if (!(json instanceof JSONNumber))
			throw new CommunicationProtocolConstructionError(
					"Expected a JSONNumber (for PieceOMediaValue media size), found " + json, json);
		media = client.readBlockLong();
	}

	public PieceOMediaValue(InputStream data) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[65536];
		int x;
		while ((x = data.read(buff)) != -1)
			baos.write(buff, 0, x);
		media = baos.toByteArray();
	}

	public PieceOMediaValue(byte... data) {
		Objects.requireNonNull(data);
		media = data;
	}

	/**
	 * <p>
	 * This method sends <i>only</i> the auxiliary data associated with this
	 * {@link PieceOMediaValue}. It is designed to be used by a <code>Request</code>
	 * class running on the client to send auxiliary data to the server when a
	 * {@link PieceOMediaValue} is being sent as part of a request.
	 * </p>
	 * <p>
	 * The client (can) send multiple arguments when invoking (certain types of)
	 * requests. {@link CommunicationProtocolRequest The Communication Protocol
	 * specifies that} requests must first send all of their JSON data in a 'JSON
	 * package, and then send any auxiliary data that they need to send to the
	 * server. Because of this, a request needs to be able to coordinate, among all
	 * the arguments it sends to the server, all the JSON data it needs to send
	 * (first), and then what auxiliary data to send after.
	 * </p>
	 * <p>
	 * This method is designed to be used in a request's
	 * {@link CommunicationProtocolRequest#sendAuxiliaryData(CommunicationConnection)}
	 * method, so that requests can send the actual media byte data associated with
	 * a {@link PieceOMediaValue} after they've included (and sent) the
	 * {@link #toJSON() JSON data} of the {@link PieceOMediaValue} to the server.
	 * </p>
	 * <p>
	 * Typically, the code in a request class to handle sending JSON is in the
	 * {@link CommunicationProtocolRequest#build(JSONObject)} method, for example,
	 * {@link SetProfileIconRequest#build(JSONObject)}. Then the auxiliary data is
	 * sent in the
	 * {@link SetProfileIconRequest#sendAuxiliaryData(CommunicationConnection)} method.
	 * </p>
	 * 
	 * @param connection
	 */
	@SuppressWarnings("javadoc")
	public void sendAuxiliaryData(CommunicationConnection connection) {
		connection.writeBlock(media);
	}

	@Override
	public void send(Connection connection) throws UnknownCommStateException {
		// This method is designed to be called on the server, NOT on the client. See
		// documentation.
		CommunicationProtocolType.super.send(connection);// Send the JSON data
		connection.writeBlock(media);// Send auxiliary (media) data.
	}

	@Override
	public JSONValue json() {
		return new JSONNumber(media.length);
	}

	/**
	 * Returns a {@link PieceOMediaValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link PieceOMediaValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link PieceOMediaValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link PieceOMediaValue}, whichever represents
	 *         the provided argument.
	 * @throws BlockException            If a {@link BlockException} occurs while
	 *                                   reading the piece of media from the stream.
	 *                                   This cannot be thrown if the provided
	 *                                   argument is {@link JSONConstant#NULL}.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs while reading the piece of media
	 *                                   from the stream. This cannot be thrown if
	 *                                   the provided argument is
	 *                                   {@link JSONConstant#NULL}.
	 */
	public static PieceOMediaValue fromNullable(JSONValue value, Connection client)
			throws UnknownCommStateException, BlockException {
		return value == JSONConstant.NULL ? null : new PieceOMediaValue(value, client);
	}

	public static PieceOMediaValue fromNullable(JSONValue value, CommunicationConnection client) {
		return value == JSONConstant.NULL ? null : new PieceOMediaValue(value, client);
	}

}
