package pala.apps.arlith.libraries.networking;

import pala.apps.arlith.libraries.streams.InputStream;
import pala.apps.arlith.libraries.streams.OutputStream;
import pala.libs.generic.json.JSONValue;

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