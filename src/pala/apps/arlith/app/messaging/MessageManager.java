package pala.apps.arlith.app.messaging;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import pala.apps.arlith.backend.streams.IOStream;
import pala.apps.arlith.backend.streams.InputStream.IllegalByteException;
import pala.apps.arlith.backend.streams.InputStream.StringTooLargeException;
import pala.libs.generic.json.JSONValue;

/**
 * Synchronizes write and read operations and provides a higher level method for
 * sending messages.
 * 
 * @author Palanath
 *
 */
public class MessageManager {
	private final IOStream stream;

	public MessageManager(IOStream stream) {
		synchronized (stream) {
			this.stream = stream;
		}
	}

	public void write(int b) throws IOException {
		synchronized (stream) {
			stream.write(b);
		}
	}

	public void write(byte[] arr) throws IOException {
		synchronized (stream) {
			stream.write(arr);
		}
	}

	public void write(byte[] arr, int offset, int length) throws IOException {
		synchronized (stream) {
			stream.write(arr, offset, length);
		}
	}

	public int read() throws IOException {
		synchronized (stream) {
			return stream.read();
		}
	}

	public int fill(byte[] arr) throws IOException {
		synchronized (stream) {
			return stream.fill(arr);
		}
	}

	public int fill(byte[] arr, int offset, int len) throws IOException {
		synchronized (stream) {
			return stream.fill(arr, offset, len);
		}
	}

	public int read(byte[] arr) throws IOException {
		synchronized (stream) {
			return stream.read(arr);
		}
	}

	public int read(byte[] bytes, int offset, int len) throws IOException {
		synchronized (stream) {
			return stream.read(bytes, offset, len);
		}
	}

	public String readStringShort(Charset cs, int maxLenBytes)
			throws IOException, EOFException, StringTooLargeException {
		synchronized (stream) {
			return stream.readStringShort(cs, maxLenBytes);
		}
	}

	public int writeStringShort(String str, Charset cs) throws IOException {
		synchronized (stream) {
			return stream.writeStringShort(str, cs);
		}
	}

	public String readStringShort(Charset cs) throws EOFException, IOException, StringTooLargeException {
		synchronized (stream) {
			return stream.readStringShort(cs);
		}
	}

	public String readStringLong(Charset cs, int maxLenBytes)
			throws IOException, EOFException, StringTooLargeException {
		synchronized (stream) {
			return stream.readStringLong(cs, maxLenBytes);
		}
	}

	public int writeStringLong(String str, Charset cs) throws IOException {
		synchronized (stream) {
			return stream.writeStringLong(str, cs);
		}
	}

	public boolean readBoolean() throws EOFException, IOException, IllegalByteException {
		synchronized (stream) {
			return stream.readBoolean();
		}
	}

	public void writeBoolean(boolean b) throws IOException {
		synchronized (stream) {
			stream.writeBoolean(b);
		}
	}

	public short readShort() throws IOException {
		synchronized (stream) {
			return stream.readShort();
		}
	}

	public void writeShort(short s) throws IOException {
		synchronized (stream) {
			stream.writeShort(s);
		}
	}

	public void writeInteger(int i) throws IOException {
		synchronized (stream) {
			stream.writeInteger(i);
		}
	}

	public int readInt() throws IOException {
		synchronized (stream) {
			return stream.readInt();
		}
	}

	public void writeJSON(JSONValue json, Charset cs) throws IOException {
		synchronized (stream) {
			stream.writeJSON(json, cs);
		}
	}

	public void write(ByteBuffer buff) throws IOException {
		synchronized (stream) {
			stream.write(buff);
		}
	}

	public String readStringLong(Charset cs) throws EOFException, IOException, StringTooLargeException {
		synchronized (stream) {
			return stream.readStringLong(cs);
		}
	}

	public JSONValue readJSON(Charset cs) throws EOFException, IOException, StringTooLargeException {
		synchronized (stream) {
			return stream.readJSON(cs);
		}
	}

	public JSONValue readJSON(Charset cs, int maxLen) throws EOFException, IOException, StringTooLargeException {
		synchronized (stream) {
			return stream.readJSON(cs, maxLen);
		}
	}

	public void writeMessage(String type, String content) {

	}

}
