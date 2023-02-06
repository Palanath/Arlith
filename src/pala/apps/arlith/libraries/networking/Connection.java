package pala.apps.arlith.libraries.networking;

import java.net.Socket;

import pala.apps.arlith.libraries.networking.scp.CommunicationConnection;
import pala.apps.arlith.libraries.streams.InputStream;
import pala.apps.arlith.libraries.streams.OutputStream;
import pala.libs.generic.json.JSONValue;

/**
 * <p>
 * {@link Connection}s streamline communication through a {@link Socket} by
 * providing the ability to communicate data back and forth using blocks. A
 * single block can be sent or read at a time. Blocks remain disjoint after
 * they're sent, so if {@link #writeBlock(byte[])} is called with two arrays,
 * the first being 50 bytes large and the second being 125, when the receiver
 * calls #{@link #readBlockLong()} twice, the receiver will receive the 50 byte
 * array and then the 125 byte array.
 * </p>
 * <p>
 * The primary function of this interface is to provide an API for block-based
 * communication. For implementations, see {@link Communicator} and
 * {@link CommunicationConnection}.
 * </p>
 * 
 * @author Palanath
 *
 */
public interface Connection {

	byte[] readBlockShort() throws UnknownCommStateException, BlockException;

	byte[] readBlockShort(short maxLen) throws UnknownCommStateException, BlockException;

	byte[] readBlockLong() throws UnknownCommStateException, BlockException;

	byte[] readBlockLong(int maxLen) throws UnknownCommStateException, BlockException;

	void writeBlockShort(byte[] b) throws UnknownCommStateException;

	void writeBlock(byte[] b) throws UnknownCommStateException;

	void writeVariableBlock(InputStream is) throws UnknownCommStateException;

	void writeVariableBlock(byte[] b) throws UnknownCommStateException;

	void readVariableBlock(OutputStream acceptor) throws UnknownCommStateException;

	void sendStringShort(String s) throws UnknownCommStateException;

	void sendString(String s) throws UnknownCommStateException;

	String readStringShort() throws UnknownCommStateException, BlockException;

	String readStringShort(int lim) throws UnknownCommStateException, BlockException;

	String readString() throws UnknownCommStateException, BlockException;

	String readString(int lim) throws UnknownCommStateException, BlockException;

	void sendJSON(JSONValue value) throws UnknownCommStateException;

	JSONValue readJSON() throws UnknownCommStateException, BlockException;

	JSONValue readJSON(int lim) throws UnknownCommStateException, BlockException;

	/**
	 * Closes this {@link Connection}.
	 * 
	 * @author Palanath
	 */
	void close();

}