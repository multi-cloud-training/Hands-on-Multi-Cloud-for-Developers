package cz.zcu.kiv.multicloud.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.utils/SecureFileCredentialStore.java			<br /><br />
 *
 * Implementation of the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} using simple {java.io.File} as a base for storing the tokens.
 * This implementation uses AES file encryption for improving security of stored tokens. Use of default password is not advised.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SecureFileCredentialStore extends FileCredentialStore implements CredentialStore {

	/** Default password to use. */
	protected static final String DEFAULT_PASSWORD = "f88e2aWtUBn7lPewG37H1a60kA9Qpl6D";
	/** Default salt to use. */
	protected static final byte[] DEFAULT_SALT = "d9EQ2xRl".getBytes();

	/** Password to use. */
	private final char[] password;
	/** Salt to use. */
	private final byte[] salt = DEFAULT_SALT;

	/**
	 * Ctor with supplied {@link java.io.File} of the credential store.
	 * @param file Credential store file.
	 */
	public SecureFileCredentialStore(File file) {
		this(file, DEFAULT_PASSWORD);
	}

	/**
	 * Ctor with supplied {@link java.io.File} and password of the credential store.
	 * @param file Credential store file.
	 * @param password Password of the store.
	 */
	public SecureFileCredentialStore(File file, String password) {
		super(file, DEFAULT_SERIALIZATION);
		this.password = password.toCharArray();
		load();
	}

	/**
	 * Ctor with supplied path to the credential store file.
	 * @param file Path to the file.
	 */
	public SecureFileCredentialStore(String file) {
		this(new File(file), DEFAULT_PASSWORD);
	}

	/**
	 * Ctor with supplied path to the credential store file and password of the credential store.
	 * @param file Path to the file.
	 * @param password Password of the store.
	 */
	public SecureFileCredentialStore(String file, String password) {
		this(new File(file), password);
	}

	/**
	 * Loads the file with credentials and decrypts token data.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void load() {
		if (!credentialFile.exists() || password == null || salt == null) {
			return;
		}
		FileInputStream fis = null;
		CipherInputStream cis = null;
		ObjectInputStream ois = null;
		try {
			byte[] ivBytes = new byte[16];
			/* read IV */
			fis = new FileInputStream(credentialFile);
			fis.read(ivBytes);
			/* prepare decryption key */
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret, iv);
			/* read encrypted data */
			cis = new CipherInputStream(fis, cipher);
			ois = new ObjectInputStream(cis);
			tokens = (Map<String, OAuth2Token>) ois.readObject();
		} catch (GeneralSecurityException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
				if (cis != null) {
					cis.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				/* ignore closing exception */
			}
		}
	}

	/**
	 * Saves the file with encrypted credentials.
	 */
	@Override
	protected void save() {
		FileOutputStream fos = null;
		CipherOutputStream cos = null;
		ObjectOutputStream oos = null;
		try {
			/* prepare encryption key */
			SecureRandom rnd = new SecureRandom();
			byte[] ivBytes = new byte[16];
			rnd.nextBytes(ivBytes);
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec(password, salt, 1024, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
			/* write IV */
			fos = new FileOutputStream(credentialFile);
			fos.write(ivBytes);
			/* write encrypted data */
			cos = new CipherOutputStream(fos, cipher);
			oos = new ObjectOutputStream(cos);
			oos.writeObject(tokens);
		} catch (GeneralSecurityException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (cos != null) {
					cos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				/* ignore closing exception */
			}
		}
	}

}
