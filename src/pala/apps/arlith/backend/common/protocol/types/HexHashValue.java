package pala.apps.arlith.backend.common.protocol.types;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import pala.apps.arlith.backend.common.protocol.meta.CommunicationProtocolConstructionError;
import pala.libs.generic.json.JSONConstant;
import pala.libs.generic.json.JSONString;
import pala.libs.generic.json.JSONValue;
import pala.libs.generic.strings.StringTools;

public final class HexHashValue implements CommunicationProtocolType {

	public String getHash() {
		return StringTools.toHexString(bytes);
	}

	private static String hashHexStr(String input) {
		return hashHexStr(input.getBytes(StandardCharsets.UTF_8));
	}

	private static byte[] hash(byte... input) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(input);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(
					"The SHA-256 algorithm could not be found in the current Java Runtime, even though every Java Runtime is supposed to come with an implementation of it. This means that values like passwords cannot be hashed. Since safety requires that they be hashed prior to sending to the server, the client cannot send a password over to the server.");
		}
	}

	private static String hashHexStr(byte... input) {
		return StringTools.toHexString(hash(input));
	}

	private final byte[] bytes;

	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	public static HexHashValue createAndHash(String unhashedInput) {
		return new HexHashValue(unhashedInput, true);
	}

	public static HexHashValue createAlreadyHashed(String hashedInput) {
		return new HexHashValue(hashedInput);
	}

	public HexHashValue(String hashedString) {
		bytes = StringTools.fromHexString(hashedString);
	}

	public HexHashValue(byte... bytes) {
		this(hashHexStr(bytes));
	}

	public HexHashValue(String string, boolean hash) {
		this(hash ? hashHexStr(string) : string);
	}

	public HexHashValue(JSONValue json) {
		if (!(json instanceof JSONString))
			throw new CommunicationProtocolConstructionError("Expected a HexHash, but found: " + json, json);
		bytes = StringTools.fromHexString(((JSONString) json).getValue());
	}

	@Override
	public JSONString json() {
		return new JSONString(getHash());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof HexHashValue && Arrays.equals(bytes, ((HexHashValue) obj).bytes);
	}

	@Override
	public String toString() {
		return getHash();
	}

	/**
	 * Returns a {@link HexHashValue} representing the provided argument if the
	 * provided argument is not {@link JSONConstant#NULL}, otherwise, returns
	 * <code>null</code>. This is essentially the "<code>null</code>-safe"
	 * <code>from</code> method for {@link HexHashValue}s.
	 * 
	 * @param value The {@link JSONValue} to get the {@link HexHashValue} from,
	 *              which may represent <code>null</code> (by being
	 *              {@link JSONConstant#NULL}).
	 * @return <code>null</code> or a {@link HexHashValue}, whichever represents the
	 *         provided argument.
	 */
	public static HexHashValue fromNullable(JSONValue value) {
		return value == JSONConstant.NULL ? null : new HexHashValue(value);
	}

}
