package pala.apps.arlith.libraries.streams;

import java.io.Closeable;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import pala.libs.generic.json.JSONParser;
import pala.libs.generic.json.JSONValue;
import pala.libs.generic.streams.CharacterStream;

public interface InputStream extends Closeable {
	class StringTooLargeException extends DatumTooLargeException {

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;

		public StringTooLargeException(int actualLength, int maxLength) {
			super(actualLength, maxLength);
		}

	}

	class DatumTooLargeException extends Exception {

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;
		private final int actualLength, maxLength;

		public DatumTooLargeException(int actualLength, int maxLength) {
			this.actualLength = actualLength;
			this.maxLength = maxLength;
		}

		public int getActualLength() {
			return actualLength;
		}

		public int getMaxLength() {
			return maxLength;
		}

	}

	class IllegalByteException extends RuntimeException {

		private byte value;

		public byte getValue() {
			return value;
		}

		public void setValue(byte value) {
			this.value = value;
		}

		public IllegalByteException(byte value) {
			this.value = value;
		}

		/**
		 * SUID
		 */
		private static final long serialVersionUID = 1L;

	}

	/**
	 * Reads the next byte in this stream if any. Returns <code>-1</code> otherwise.
	 * 
	 * @return The next byte in this stream if there is one or <code>-1</code>.
	 * @throws IOException If an error occurs while reading.
	 */
	int read() throws IOException;

	/**
	 * <p>
	 * Reads bytes from this stream into the provided array until an exception
	 * occurs, the array is full, or the end of the stream is reached.
	 * </p>
	 * <p>
	 * Both of the two {@link #read(byte[])} methods that accept a byte array as a
	 * parameter will return the number of bytes read by a call to the method,
	 * (unless the end of the stream is reached). This returned value is typically
	 * equal to the size of the array, unless the end of the stream is reached
	 * (causing <code>-1</code> to be returned), but can (according to
	 * {@link InputStream} implementation) be less the size of the array and greater
	 * than <code>-1</code>. This event denotes that either (1) all of the remaining
	 * data in the stream has been read or (2) <i>some</i> of the remaining data has
	 * been read and there is more to be read that simply did not get read from the
	 * single {@link #read(byte[])} call. This {@link #fill(byte[])} method behaves
	 * very similarly to its corresponding {@link #read(byte[])} method except that
	 * when it returns a value that is less than the length of the provided array
	 * and not <code>-1</code>, it means that the end of the stream has been
	 * reached.
	 * 
	 * @param arr The array to read data into.
	 * @return <code>-1</code> if the end of the stream has been reached <b>and</b>
	 *         no more data is available to be read, <code>arr.length</code> if
	 *         there was at least enough data remaining in the stream to be read
	 *         into the array (and so such data was read into the provided array),
	 *         or any number in between, except <code>0</code>, to indicate that all
	 *         of the data that could be read from the stream was read into the
	 *         provided array.
	 * @throws IOException If an error occurs while reading.
	 */
	default int fill(byte[] arr) throws IOException {
		return fill(arr, 0, arr.length);
	}

	default int fill(byte[] arr, int offset, int len) throws IOException {
		int rem = len, rd;
		while (rem != 0 && ((rd = read(arr, offset + len - rem, rem)) != -1))
			rem -= rd;
		return len - rem;
	}

	/**
	 * Tries to fill the provided array with the bytes from this stream, in
	 * sequence. More formally, this method reads as many bytes as possible up to a
	 * maximum of the size of the provided array and fills them, in order, in the
	 * array. A call to this method is equivalent to
	 * <code>{@link #read(byte[], int, int) read(arr, 0, arr.length)}</code>.
	 * 
	 * @param arr The array to read into.
	 * @return The number of bytes read, or <code>-1</code> if there are no more
	 *         bytes to be read. This may be less than the size of the array, and
	 *         can even be <code>0</code>. If it is desired that the array be filled
	 *         when possible, {@link #fill(byte[])} may be used instead.
	 * @throws IOException If an error occurs while reading.
	 */
	default int read(byte[] arr) throws IOException {
		return read(arr, 0, arr.length);
	}

	default byte[] readBlockShort() throws IOException {
		byte[] b = new byte[readShort()];
		fill(b);
		return b;
	}

	default byte[] readBlockShort(short maxLen) throws IOException, DatumTooLargeException {
		short len = readShort();
		if (len > maxLen)
			throw new DatumTooLargeException(len, maxLen);
		byte[] b = new byte[len];
		fill(b);
		return b;
	}

	default byte[] readBlockLong() throws IOException {
		byte[] b = new byte[readInt()];
		fill(b);
		return b;
	}

	default byte[] readBlockLong(int maxLen) throws IOException, DatumTooLargeException {
		int len = readInt();
		if (len > maxLen)
			throw new DatumTooLargeException(len, maxLen);
		byte[] b = new byte[len];
		fill(b);// TODO Convert to while(read()) impl.
		return b;
	}

	default int read(byte[] bytes, int offset, int len) throws IOException {
		for (int i = offset; i < len + offset; i++) {
			int read = read();
			if (read == -1) {
				int diff = i - offset;
				return diff == 0 ? -1 : diff;
			}
			bytes[i] = (byte) read;
		}
		return len;
	}

	static InputStream fromJavaInputStream(java.io.InputStream jis) {
		return new InputStream() {

			@Override
			public int read() throws IOException {
				return jis.read();
			}

			@Override
			public int read(byte[] arr) throws IOException {
				return jis.read(arr);
			}

			@Override
			public int read(byte[] bytes, int offset, int len) throws IOException {
				return jis.read(bytes, offset, len);
			}

			@Override
			public void close() throws IOException {
				jis.close();
			}

		};
	}

	static InputStream fromJavaInputStream(FileInputStream jis) {
		class DIS implements InputStream, Determinate {

			@Override
			public int read() throws IOException {
				return jis.read();
			}

			@Override
			public int read(byte[] arr) throws IOException {
				return jis.read(arr);
			}

			@Override
			public int read(byte[] bytes, int offset, int len) throws IOException {
				return jis.read(bytes, offset, len);
			}

			@Override
			public void close() throws IOException {
				jis.close();
			}

			@Override
			public int length() {
				try {
					return (int) (jis.getChannel().size() - jis.getChannel().position());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}
		return new DIS();
	}

	/**
	 * Returns a Java {@link java.io.InputStream} that, when closed, does nothing.
	 * Its 3 <code>read</code> methods delegate to this {@link InputStream}'s
	 * <code>read</code> methods.
	 * 
	 * @return The newly created Java {@link java.io.InputStream}.
	 */
	default java.io.InputStream toJavaInputStream() {
		return new java.io.InputStream() {

			@Override
			public int read() throws IOException {
				return InputStream.this.read();
			}

			@Override
			public int read(byte[] b) throws IOException {
				return InputStream.this.read(b);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return InputStream.this.read(b, off, len);
			}
		};
	}

	@Override
	default void close() throws IOException {
	}

	default String readStringShort(Charset cs, int maxLenBytes)
			throws IOException, EOFException, StringTooLargeException {
		int len = readShort();
		if (len > maxLenBytes)
			throw new StringTooLargeException(len, maxLenBytes);
		byte[] b = new byte[len];
		fill(b);
		return new String(b, cs);
	}

	default String readStringShort(Charset cs) throws EOFException, IOException, StringTooLargeException {
		return readStringShort(cs, Integer.MAX_VALUE);
	}

	default String readStringLong(Charset cs, int maxLenBytes)
			throws IOException, EOFException, StringTooLargeException {
		int len = readInt();
		if (len > maxLenBytes)
			throw new StringTooLargeException(len, maxLenBytes);
		byte[] b = new byte[len];
		fill(b);
		return new String(b, cs);
	}

	default boolean readBoolean() throws EOFException, IOException, IllegalByteException {
		int v = read();
		switch (v) {
		case -1:
			throw new EOFException();
		case 0:
			return false;
		case 1:
			return true;
		default:
			throw new IllegalByteException((byte) v);
		}
	}

	default short readShort() throws IOException {
		int f = read();// If this is -1 then the next read() will be too.
		short res = (short) (f << 8);
		f = read();
		if (f == -1)
			throw new EOFException();
		return (short) (res | (byte) f);
	}

	default int readInt() throws IOException {
		int f = read();
		f = f << 8 | read();
		f = f << 8 | read();
		int z = read();
		if (z == -1)
			throw new EOFException();
		return f << 8 | z;
	}

	default String readStringLong(Charset cs) throws EOFException, IOException, StringTooLargeException {
		return readStringLong(cs, Integer.MAX_VALUE);
	}

	default JSONValue readJSON(Charset cs) throws EOFException, IOException, StringTooLargeException {
		return readJSON(cs, Integer.MAX_VALUE);
	}

	default JSONValue readJSON(Charset cs, int maxLen) throws EOFException, IOException, StringTooLargeException {
		return new JSONParser().parse(CharacterStream.from(readStringLong(cs, maxLen)));
	}

	static InputStream from(byte... media) {
		class BIS implements InputStream, Determinate {

			int i = 0;

			@Override
			public int read() throws IOException {
				return i >= media.length ? -1 : 255 & media[i++];
			}

			@Override
			public int read(byte[] bytes, int offset, int len) throws IOException {
				len = len > media.length - i ? media.length - i : len;
				System.arraycopy(media, i, bytes, offset, len);
				return len;
			}

			@Override
			public int length() {
				return media.length - i;
			}

		}
		return new BIS();
	}

}
