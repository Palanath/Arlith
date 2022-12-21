package pala.apps.arlith.libraries.networking;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pala.apps.arlith.backend.streams.IOStream;
import pala.apps.arlith.backend.streams.InputStream;
import pala.apps.arlith.backend.streams.OutputStream;
import pala.apps.arlith.backend.streams.InputStream.DatumTooLargeException;
import pala.apps.arlith.libraries.networking.encryption.EncryptedConnection;
import pala.apps.arlith.libraries.networking.encryption.MalformedResponseException;
import pala.libs.generic.json.JSONParser;
import pala.libs.generic.json.JSONValue;
import pala.libs.generic.streams.CharacterStream;

/**
 * <p>
 * A class that streamlines communication through a {@link Socket} by providing
 * (only) the ability to communicate back and forth using blocks of raw byte
 * data. (The restriction is imposed because of encryption.) A single block can
 * be sent or read at a time. Blocks remain disjoint after they're sent, so if
 * {@link #writeBlock(byte[])} is called with two arrays, the first being 50
 * bytes large and the second being 125, when the receiver calls
 * #{@link #readBlockLong()} twice, the receiver will receive the 50 byte array
 * and then the 125 byte array.
 * </p>
 * <p>
 * Please note that any read methods in this class that make use of a limit
 * parameter will use that parameter to limit the amount of <b>raw data</b> that
 * the method attempts to read in a single block. This class automatically
 * encrypts outgoing data and decrypts incoming data, but due to the fact that
 * decryption can need a "whole chunk" (full block) of data before it can take
 * place, the limit, (which is intended for cutting connections if too much data
 * is sent), will have no real purpose on the <i>result</i> of the decryption,
 * but rather the bytes that get decrypted. The encryption algorithms in this
 * class sometimes result in a raw number of bytes that is larger (or possibly
 * smaller) than the input, (meaning that a call to {@link #writeBlock(byte[])}
 * with an array of size 50 may actually write 60 bytes of data, in a single
 * block, to the stream. In this case, if the reader used a limit of 50, the
 * read operation would fail).
 * </p>
 * <p>
 * Objects of this class cannot be "reopened" once they've become
 * {@link #close() closed}. The close method in this class is automatically
 * called whenever an {@link UnknownCommStateException} is thrown from any
 * method. By default, it closes the underlying {@link IOStream} that this
 * {@link Communicator} uses to communicate, but such functionality can be
 * overridden to provide more functionality upon a forceful close.
 * </p>
 * 
 * @author Palanath
 *
 */
public class Communicator implements Connection {

	private final IOStream ios;
	private final EncryptedConnection enccon;

	public Communicator(Socket sock) throws IOException, InvalidKeyException, InvalidKeySpecException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, MalformedResponseException {
		ios = IOStream.fromSocket(sock);
		enccon = new EncryptedConnection(sock.getInputStream(), sock.getOutputStream());
	}

	private byte[] decrypt(byte[] input) throws BadPaddingException {
		try {
			return enccon.getIn().doFinal(input);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);// Shouldn't happen.
		}
	}

	private byte[] encrypt(byte[] input) {
		try {
			return enccon.getOut().doFinal(input);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);// Shouldn't happen.
		}
	}

	@Override
	public byte[] readBlockShort() throws UnknownCommStateException, BlockException {
		byte[] data;
		try {
			data = ios.readBlockShort();
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
		try {
			return decrypt(data);
		} catch (BadPaddingException e) {
			throw new BlockException(e, data);
		}
	}

	@Override
	public byte[] readBlockShort(short maxLen) throws UnknownCommStateException, BlockException {
		byte[] data;
		try {
			data = ios.readBlockShort(maxLen);
		} catch (IOException | DatumTooLargeException e) {
			throw new UnknownCommStateException(e);
		}
		try {
			return decrypt(data);
		} catch (BadPaddingException e) {
			throw new BlockException(e, data);
		}
	}

	@Override
	public byte[] readBlockLong() throws UnknownCommStateException, BlockException {
		byte[] data;
		try {
			data = ios.readBlockLong();
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
		try {
			return decrypt(data);
		} catch (BadPaddingException e) {
			throw new BlockException(e, data);
		}
	}

	@Override
	public byte[] readBlockLong(int maxLen) throws UnknownCommStateException, BlockException {
		byte[] data;
		try {
			data = ios.readBlockLong(maxLen);
		} catch (IOException | DatumTooLargeException e) {
			throw new UnknownCommStateException(e);
		}
		try {
			return decrypt(data);
		} catch (BadPaddingException e) {
			throw new BlockException(e, data);
		}
	}

	@Override
	public void writeBlockShort(byte[] b) throws UnknownCommStateException {
		try {
			ios.writeBlockShort(encrypt(b));
			ios.flush();
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
	}

	@Override
	public void writeBlock(byte[] b) throws UnknownCommStateException {
		try {
			ios.writeBlock(encrypt(b));
			ios.flush();
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
	}

	private static byte[] variablize(byte[] input, int len) {
		len = Math.min(input.length, len);
		int i = len;
		for (byte by : input)
			if (by == 0 || by == Byte.MAX_VALUE)
				i++;
		byte[] outputBlock = new byte[i];
		int pivot = 0, outputPos = 0;

		for (i = 0; i < len; i++) {
			if (input[i] == 0) {
				System.arraycopy(input, pivot, outputBlock, outputPos, i - pivot);
				outputPos += i - pivot;
				pivot = i;
				outputBlock[outputPos] = Byte.MAX_VALUE;
				outputBlock[++outputPos] = 0;
			} else if (input[i] == Byte.MAX_VALUE) {
				System.arraycopy(input, pivot, outputBlock, outputPos, i - pivot);
				outputPos += i - pivot;
				pivot = i;
				outputBlock[outputPos] = Byte.MAX_VALUE;
				outputBlock[++outputPos] = Byte.MAX_VALUE;
			}
		}
		System.arraycopy(input, pivot, outputBlock, outputPos, i - pivot);
		return outputBlock;
	}

	private static byte[] variablize(byte[] input) {
		return variablize(input, input.length);
	}

	@Override
	public void writeVariableBlock(InputStream is) throws UnknownCommStateException {
		try {
			int amt;
			byte[] buff = new byte[65536];
			while ((amt = is.read(buff)) >= 0)
				if (amt > 0) {
					byte[] res = enccon.getOut().update(buff, 0, amt);
					res = variablize(res);
					ios.write(res);
				}
			byte[] res = enccon.getOut().doFinal();
			if (res.length != 0) {
				res = variablize(res);
				ios.write(res);
			}
			ios.write(0);
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void writeVariableBlock(byte[] b) throws UnknownCommStateException {
		try {
			ios.write(variablize(b, b.length));
			ios.write(0);
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
	}

	@Override
	public void readVariableBlock(OutputStream acceptor) throws UnknownCommStateException {
		try {
			byte[] bytes = new byte[65536];
			int pos = 0;
			while (true) {
				int i = ios.read();
				if (i < 0)
					throw new EOFException();
				switch (i) {
				default:
					bytes[pos++] = (byte) i;
					if (pos >= bytes.length) {
						acceptor.write(enccon.getIn().update(bytes));
						pos = 0;
					}
					break;
				case Byte.MAX_VALUE:
					i = ios.read();
					if (i < 0)
						throw new EOFException();
					bytes[pos++] = (byte) i;
					if (i != 0 && i != Byte.MAX_VALUE)
						System.out.println(i);
					if (pos >= bytes.length) {
						acceptor.write(enccon.getIn().update(bytes));
						pos = 0;
					}
					break;
				case 0:
					try {
						if (pos == 0)
							acceptor.write(enccon.getIn().doFinal());
						else {
							acceptor.write(enccon.getIn().doFinal(bytes, 0, pos));
						}
					} catch (IllegalBlockSizeException | BadPaddingException e) {
						throw new RuntimeException(e);
					}
					acceptor.flush();
					return;
				}
			}
		} catch (IOException e) {
			throw new UnknownCommStateException(e);
		}
	}

	@Override
	public void sendStringShort(String s) throws UnknownCommStateException {
		writeBlockShort(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void sendString(String s) throws UnknownCommStateException {
		writeBlock(s.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String readStringShort() throws UnknownCommStateException, BlockException {
		return new String(readBlockShort(), StandardCharsets.UTF_8);
	}

	@Override
	public String readStringShort(int lim) throws UnknownCommStateException, BlockException {
		return new String(readBlockLong(lim), StandardCharsets.UTF_8);
	}

	@Override
	public String readString() throws UnknownCommStateException, BlockException {
		return new String(readBlockLong(), StandardCharsets.UTF_8);
	}

	@Override
	public String readString(int lim) throws UnknownCommStateException, BlockException {
		return new String(readBlockLong(lim), StandardCharsets.UTF_8);
	}

	@Override
	public void sendJSON(JSONValue value) throws UnknownCommStateException {
		sendString(value.toString());
	}

	@Override
	public JSONValue readJSON() throws UnknownCommStateException, BlockException {
		return new JSONParser().parse(CharacterStream.from(readString()));
	}

	@Override
	public JSONValue readJSON(int lim) throws UnknownCommStateException, BlockException {
		return new JSONParser().parse(CharacterStream.from(readString(lim)));
	}

	/**
	 * By implementation in the {@link Communicator} class, this method simply
	 * closes the underlying {@link IOStream} via its {@link IOStream#close()
	 * close()} method. Any {@link IOException} thrown from the running of such
	 * operation is caught and wrapped in a {@link RuntimeException}.
	 * 
	 * @throws RuntimeException In case closing of the underlying {@link IOStream}
	 *                          results in an {@link IOException}.
	 * @author Palanath
	 */
	@Override
	public void close() throws RuntimeException {
		try {
			ios.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
