package pala.apps.arlith.libraries.streams;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import pala.libs.generic.json.JSONValue;

public interface OutputStream extends Closeable {
	void write(int b) throws IOException;

	default void write(byte[] arr) throws IOException {
		write(arr, 0, arr.length);
	}

	default void write(byte[] arr, int offset, int length) throws IOException {
		length = length + offset;
		for (; offset < length; offset++)
			write(arr[offset]);
	}

	static OutputStream fromJavaOutputStream(java.io.OutputStream jos) {
		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				jos.write(b);
			}

			@Override
			public void write(byte[] arr) throws IOException {
				jos.write(arr);
			}

			@Override
			public void write(byte[] arr, int offset, int length) throws IOException {
				jos.write(arr, offset, length);
			}

			@Override
			public void flush() throws IOException {
				jos.flush();
			}

			@Override
			public void close() throws IOException {
				jos.close();
			}

		};
	}

	/**
	 * Returns a Java {@link java.io.OutputStream} that, upon being closed, does
	 * nothing. Its <code>write</code> methods delegate to this
	 * {@link OutputStream}'s <code>write</code> methods. Flushing the given
	 * {@link java.io.OutputStream} simply calls this {@link OutputStream}'s
	 * {@link OutputStream#flush() flush()} method.
	 * 
	 * @return The new Java {@link java.io.OutputStream}.
	 */
	default java.io.OutputStream toJavaOutputStream() {
		return new java.io.OutputStream() {

			@Override
			public void write(int b) throws IOException {
				OutputStream.this.write(b);
			}

			@Override
			public void write(byte[] b) throws IOException {
				OutputStream.this.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				OutputStream.this.write(b, off, len);
			}

			@Override
			public void flush() throws IOException {
				OutputStream.this.flush();
			}
		};
	}

	@Override
	default void close() throws IOException {
	}

	default void flush() throws IOException {
	}

	class StringTooLargeException extends RuntimeException {

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;
		private final String string;
		private final byte[] bytes;
		private final Charset charset;

		public byte[] getBytes() {
			return bytes;
		}

		public Charset getCharset() {
			return charset;
		}

		public String getString() {
			return string;
		}

		public StringTooLargeException(String message, String string, byte[] bytes, Charset charset) {
			super(message);
			this.string = string;
			this.bytes = bytes;
			this.charset = charset;
		}

	}

	/**
	 * Writes a string encoded with the specified encoding.
	 * 
	 * @param str The {@link String} to write.
	 * @param cs  The {@link Charset} to use for the conversion from characters to
	 *            bytes. (Defines what bytes each character gets encoded to.)
	 * @throws IOException If an {@link IOException} occurs.
	 */
	default int writeStringShort(String str, Charset cs) throws IOException {
		byte[] b = str.getBytes(cs);
		if (b.length > 65535)
			throw new StringTooLargeException("String too large to be sent over IOStream.", str, b, cs);
		write(b.length >> 8 & 0xFF);
		write(b.length & 0xFF);
		write(b);
		return 2 + b.length;
	}

	default int writeStringLong(String str, Charset cs) throws IOException {
		byte[] b = str.getBytes(cs);
		writeInteger(b.length);
		write(b);
		return 4 + b.length;
	}

	/**
	 * Writes a boolean as a whole byte.
	 * 
	 * @param b The boolean.
	 * @throws IOException If an {@link IOException} occurs.
	 */
	default void writeBoolean(boolean b) throws IOException {
		write(b ? 1 : 0);
	}

	default void writeShort(short s) throws IOException {
		write(s >> 8 & 0xFF);
		write(s & 0xFF);
	}

	default void writeInteger(int i) throws IOException {
		write(i >> 24 & 0xFF);
		write(i >> 16 & 0xFF);
		write(i >> 8 & 0xFF);
		write(i & 0xFF);
	}

	default void writeJSON(JSONValue json, Charset cs) throws IOException {
		writeStringLong(JSONValue.toStringShort(json), cs);
	}

	default void writeBlockShort(byte[] b) throws IOException {
		if (b.length > Short.MAX_VALUE)
			throw new IllegalArgumentException();
		writeShort((short) b.length);
		write(b);
	}

	default void writeBlock(byte[] b) throws IOException {
		writeInteger(b.length);
		write(b);
	}

	default void writeBlockLong(byte[] b) throws IOException {
		writeBlock(b);
	}

	/**
	 * Writes a raw {@link ByteBuffer}. This simply copies the bytes in the buffer
	 * to the stream. The position is taken into account during the copy.
	 * 
	 * @param buff The {@link ByteBuffer} to copy.
	 * @throws IOException If an {@link IOException} occurs.
	 */
	default void write(ByteBuffer buff) throws IOException {
		if (buff.hasArray())
			write(buff.array(), buff.arrayOffset() + buff.position(), buff.remaining());
		else {
			byte[] b = new byte[buff.remaining()];
			buff.get(b);
			write(b);
		}
	}

}
