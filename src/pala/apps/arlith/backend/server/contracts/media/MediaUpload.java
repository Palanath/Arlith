package pala.apps.arlith.backend.server.contracts.media;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pala.apps.arlith.backend.networking.BlockException;
import pala.apps.arlith.backend.networking.Connection;
import pala.apps.arlith.backend.networking.UnknownCommStateException;
import pala.apps.arlith.backend.streams.OutputStream;

public interface MediaUpload {
	/**
	 * Returns the {@link Connection} that the media is sent over. The next read
	 * operation performed on this {@link Connection} should be an attempt to read
	 * the uploaded media. The uploaded media is the next block on the
	 * {@link Connection}.
	 * 
	 * @return The {@link Connection} over which to read the media.
	 */
	Connection connection();

	/**
	 * <p>
	 * Whether the media is variable in size or is a single, long-block. This method
	 * should be called by {@link #writeTo(OutputStream)} or by code reading the
	 * {@link #connection()} to determine how the media should be read.
	 * </p>
	 * <p>
	 * If this method returns <code>true</code>, then the media should be read from
	 * the {@link #connection()} using
	 * {@link Connection#readVariableBlock(OutputStream)}. Otherwise, it should be
	 * read using {@link Connection#readBlockLong()} (or
	 * {@link Connection#readBlockLong(int)}, if a maximum bound on the raw size in
	 * bytes is known).
	 * </p>
	 * 
	 * @return <code>true</code> if the media is variable in length and
	 *         <code>false</code> if it is not.
	 */
	boolean variable();

	/**
	 * <p>
	 * Writes the media in this {@link MediaUpload} to the specified
	 * {@link OutputStream}. If a failure occurs, the data written thus far should
	 * be considered garbage.
	 * </p>
	 * <p>
	 * It might be the case that this can only be called once, as this
	 * {@link MediaUpload} may wrap a single object on the network, and repeated
	 * calls to this method may attempt to read multiple different blocks from the
	 * network.
	 * </p>
	 * 
	 * @param out The {@link OutputStream} to write the media to.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs while reading.
	 * @throws IOException               If an {@link IOException} occurs while
	 *                                   writing to the {@link OutputStream}.
	 * @throws BlockException            If a {@link BlockException} occurs while
	 *                                   reading.
	 */
	default void writeTo(OutputStream out) throws UnknownCommStateException, IOException, BlockException {
		if (variable())
			connection().readVariableBlock(out);
		else
			out.write(connection().readBlockLong());
	}

	static MediaUpload from(boolean variable, Connection connection) {
		return new MediaUpload() {

			@Override
			public boolean variable() {
				return variable;
			}

			@Override
			public Connection connection() {
				return connection;
			}
		};
	}

	default void writeTo(java.io.OutputStream fos) throws UnknownCommStateException, IOException, BlockException {
		writeTo(OutputStream.fromJavaOutputStream(fos));
	}

	/**
	 * <p>
	 * Reads this piece of media into a {@link ByteArrayOutputStream}. If a failure
	 * occurs, the data written thus far should be considered garbage.
	 * </p>
	 * <p>
	 * It might be the case that this can only be called once, as this
	 * {@link MediaUpload} may wrap a single object on the network, and repeated
	 * calls to this method may attempt to read multiple different blocks from the
	 * network.
	 * </p>
	 * 
	 * @return A <code>byte[]</code> containing all of the bytes of the read media.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs.
	 * @throws BlockException            If a {@link BlockException} occurs.
	 */
	default byte[] read() throws UnknownCommStateException, BlockException {
		if (variable()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			connection().readVariableBlock(OutputStream.fromJavaOutputStream(baos));
			return baos.toByteArray();
		} else {
			return connection().readBlockLong();
		}

	}

	/**
	 * Reads the bytes in this media from the network stream (just like
	 * {@link #read()}) but discards the read bytes. The default implementation of
	 * this method simply calls {@link #read()} and returns nothing.
	 * 
	 * @throws BlockException            If a {@link BlockException} occurs.
	 * @throws UnknownCommStateException If an {@link UnknownCommStateException}
	 *                                   occurs.
	 */
	default void discard() throws UnknownCommStateException, BlockException {
		read();
	}

}
