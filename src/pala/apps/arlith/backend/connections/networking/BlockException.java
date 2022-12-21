package pala.apps.arlith.backend.connections.networking;

import java.util.Arrays;

import pala.libs.generic.json.JSONValue;

/**
 * Used to signify that an error occurred while reading a block from a
 * {@link Communicator}, <b>but that the entire block has been successfully
 * read</b> and that it is safe to attempt another read operation on the
 * {@link Communicator} to receive the block after the failed-to-handle one.
 * 
 * @author Palanath
 *
 */
public class BlockException extends Exception {
	/**
	 * SUID
	 * 
	 * @author Palanath
	 */
	private static final long serialVersionUID = 1L;
	private final byte[] block;

	public BlockException(Throwable cause, byte[] block) {
		super(cause);
		this.block = block;
	}

	public BlockException(String message, Throwable cause, byte[] block) {
		super(message, cause);
		this.block = block;
	}

	public BlockException(String message, byte[] block) {
		super(message);
		this.block = block;
	}

	/**
	 * @return A copy of the byte array block that was the cause of this exception.
	 *         The whole block of raw byte data might be corrupt, or it may be
	 *         intact. The latter case can happen when the root cause of this
	 *         exception is that a block of plain {@link String} data was received
	 *         but a {@link JSONValue} was attempted to be read from it.
	 * @author Palanath
	 */
	public byte[] getBlock() {
		return Arrays.copyOf(block, block.length);
	}

}
