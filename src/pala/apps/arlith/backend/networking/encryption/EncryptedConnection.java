package pala.apps.arlith.backend.networking.encryption;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pala.apps.arlith.backend.streams.IOStream;

/**
 * Provides encryption functionality over a connection by building a
 * {@link CipherInputStream} and {@link CipherOutputStream} over the given
 * {@link InputStream} and {@link OutputStream}s.
 * 
 * @author Palanath
 *
 */
public class EncryptedConnection {

	private final Cipher in, out;

	public Cipher getIn() {
		return in;
	}

	public Cipher getOut() {
		return out;
	}

	/**
	 * Creates a new {@link EncryptedConnection} instance that is capable of
	 * encrypting the specified connection.
	 * 
	 * @param input  The underlying {@link InputStream}.
	 * @param output The underlying {@link OutputStream}.
	 * @throws NoSuchAlgorithmException           If the algorithms, required to be
	 *                                            contained in standard Java impls,
	 *                                            could not be found.
	 * @throws NoSuchPaddingException             If the encryption algorithms being
	 *                                            used do not support a padding
	 *                                            level that is required to be
	 *                                            supported by impls of the Java
	 *                                            platform.
	 * @throws InvalidAlgorithmParameterException This should never happen, since
	 *                                            AES is supposed to be supported by
	 *                                            all java installs.
	 */
	public EncryptedConnection(InputStream input, OutputStream output) throws InvalidKeyException, IOException,
			MalformedResponseException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		IOStream str = IOStream.fromIOStreams(pala.apps.arlith.backend.streams.InputStream.fromJavaInputStream(input),
				pala.apps.arlith.backend.streams.OutputStream.fromJavaOutputStream(output));// Stuff for ease of
																						// writing.
		//
		// RSA
		//

		KeyPairGenerator keypairGen = KeyPairGenerator.getInstance("RSA");
		keypairGen.initialize(2048);
		KeyPair kp = keypairGen.generateKeyPair();

		{
			byte[] rsaPub = kp.getPublic().getEncoded();
			str.writeShort((short) rsaPub.length);
			str.write(rsaPub);
			str.flush();
		}

		short keylen = str.readShort();
		if (keylen > 10000)
			throw new MalformedResponseException(
					"Corresponding party of connection sent an asymmetric public key that is over 10000 bytes in encoded length. The keysize used for generation is 2048 bits, so the encoded length should never be over 10000. An error was raised as a result.");
		byte[] oppPublicKey = new byte[keylen];
		str.fill(oppPublicKey);

		PublicKey opppubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(oppPublicKey));// InvalidKeySpecException
		// if key is malformed for the production of a public key by this keyfactory.

		//
		// AES
		//

		KeyGenerator aesGen = KeyGenerator.getInstance("AES");
		aesGen.init(128);
		byte[] ivbytes = new byte[16];
		new SecureRandom().nextBytes(ivbytes);
		SecretKey encKey = aesGen.generateKey();
		// Cipher for encrypting sym key.
		Cipher keysendCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		keysendCipher.init(Cipher.ENCRYPT_MODE, opppubkey);
		byte[] enckeyEncoded = encKey.getEncoded();
		enckeyEncoded = keysendCipher.doFinal(enckeyEncoded);// Encrypt enckey
		byte[] encryptedIvbytes = keysendCipher.doFinal(ivbytes);// Encrypt ivbytes
		str.writeShort((short) enckeyEncoded.length);
		str.write(enckeyEncoded);// Write the secret key we'll use for comms.
		str.writeShort((short) encryptedIvbytes.length);
		str.write(encryptedIvbytes);
		str.flush();

		keylen = str.readShort();
		if (keylen > 2000)
			throw new MalformedResponseException(
					"Corresponding party of connection sent an encrypted symmetric key that is over 10000 bytes in encrypted, encoded length. The keysize used for generation is 128 bits, so the encoded length should never be over 10000. An error was raised as a result.");
		byte[] opSecKey = new byte[keylen];
		str.fill(opSecKey);

		keylen = str.readShort();
		if (keylen > 1000)
			throw new MalformedResponseException(
					"Corresponding party of connection sent encrypted initialization vector bytes for a symmetric key that is over 1000 bytes in encrypted length. The encoded length should never be over 1000. An error was raised as a result.");
		byte[] encryptedOPIVBytes = new byte[keylen];
		str.fill(encryptedOPIVBytes);

		Cipher decOpSec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		decOpSec.init(Cipher.DECRYPT_MODE, kp.getPrivate());
		opSecKey = decOpSec.doFinal(opSecKey);
		encryptedOPIVBytes = decOpSec.doFinal(encryptedOPIVBytes);
		SecretKey decKey = new SecretKeySpec(opSecKey, "AES");

		in = Cipher.getInstance("AES/CBC/PKCS5Padding");
		out = Cipher.getInstance("AES/CBC/PKCS5Padding");
		out.init(Cipher.ENCRYPT_MODE, encKey, new IvParameterSpec(ivbytes));
		in.init(Cipher.DECRYPT_MODE, decKey, new IvParameterSpec(encryptedOPIVBytes));

	}

}
