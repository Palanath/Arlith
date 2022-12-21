package pala.apps.arlith.backend.common.gids;

import java.math.BigInteger;
import java.util.Arrays;

import pala.libs.generic.JavaTools;
import pala.libs.generic.strings.StringTools;

/**
 * <p>
 * A unique global identifier. This class stores an ID that is able to identify
 * any object in the Arlith system. GIDs are currently split into a total of 20
 * bytes.
 * </p>
 * <p>
 * <ol>
 * <li>The first 8 bytes represent an integer number (more formally, a
 * <code>long</code>) which stores the time in milliseconds, since January 1st,
 * 1970, that the object which this {@link GID} references, was created.</li>
 * <li>The next 4 bytes are used to number the object this {@link GID} refers to
 * in respect to all other Arlith objects created on the same millisecond.</li>
 * <li>The last 8 bytes are randomly generated.</li>
 * </ol>
 * 
 * @author Palanath
 *
 */
public class GID implements Comparable<GID> {
	private final byte[] bytes;

	private static final GIDProvider DEFAULT_PROVIDER = new GIDProvider();

	GID(byte... bytes) {
		this.bytes = bytes;
	}

	public static GID newGID() {
		return new GID();
	}

	public static GID fromHex(String hex) {
		return new GID(StringTools.fromHexString(hex));
	}

	public static GID fromNumber(String number) throws IllegalArgumentException {
		if (!StringTools.isNumeric(number))
			throw new IllegalArgumentException("Not a valid GID.");
		return fromNumber(new BigInteger(number));
	}

	public static GID fromBytes(byte... bytes) {
		if (bytes.length != 20)
			throw new IllegalArgumentException("GID creation input needs to be an array of bytes that is of size 20.");
		return new GID(Arrays.copyOf(bytes, bytes.length));
	}

	private static GID fromNumber(BigInteger number) {
		return new GID(Arrays.copyOfRange(number.toByteArray(), 1, 21));
	}

	/**
	 * Returns a new {@link GID} object based off of the {@link String} provided.
	 * This method serves as the counter-part to {@link #toString()}.
	 * 
	 * @param gid The {@link String} representation of a {@link GID} to use.
	 * @return A new {@link GID} object based off of the given {@link String}
	 *         representation.
	 * 
	 * @throws IllegalArgumentException If the argument cannot be parsed as a
	 *                                  {@link GID}.
	 * 
	 * @author Palanath
	 */
	public static GID fromString(String gid) throws IllegalArgumentException {
		return fromNumber(gid);
	}

	/**
	 * Calls and returns {@link #getNumber()}.
	 */
	@Override
	public String toString() {
		return getNumber();
	}

	/**
	 * Creates a brand new {@link GID} that is guaranteed to be unique against all
	 * other {@link GID}s produced from calls to this constructor on this instance
	 * of this class.
	 * 
	 * @author Palanath
	 */
	public GID() {
		this(DEFAULT_PROVIDER.genbytes());
	}

	/**
	 * @return A copy of the bytes backing this {@link GID}. These bytes can be
	 *         passed into {@link #GID(byte...)} to construct a new {@link GID} that
	 *         is essentially a copy of this one.
	 * @author Palanath
	 */
	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	/**
	 * @return A {@link String} of hexadecimal characters that represent this
	 *         {@link GID}'s bytes. The {@link String} will always be twice the
	 *         length of this {@link GID}'s bytes, in size. The bytes themselves can
	 *         be retrieved directly with {@link #getBytes()}.
	 * @author Palanath
	 */
	public String getHex() {
		return StringTools.toHexString(bytes);
	}

	public String getNumber() {
		byte[] b = new byte[21];
		System.arraycopy(bytes, 0, b, 1, 20);
		b[0] = 0x7F;
		return new BigInteger(b).toString();
	}

	@Override
	public int hashCode() {
		return 31 * Arrays.hashCode(bytes);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		return Arrays.equals(bytes, ((GID) obj).bytes);
	}

	public long timestamp() {
		return JavaTools.bytesToLong(Arrays.copyOf(bytes, 8));
	}

	@Override
	public int compareTo(GID o) {
		int c = Long.compare(timestamp(), o.timestamp());
		return c == 0 ? Integer.compare(JavaTools.bytesToInt(Arrays.copyOfRange(bytes, 8, 12)),
				JavaTools.bytesToInt(Arrays.copyOfRange(o.bytes, 8, 12))) : c;
	}

}
