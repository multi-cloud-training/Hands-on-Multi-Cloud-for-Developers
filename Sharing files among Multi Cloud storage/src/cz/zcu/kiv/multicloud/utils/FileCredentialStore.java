package cz.zcu.kiv.multicloud.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.utils/FileCredentialStore.java			<br /><br />
 *
 * Implementation of the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} using simple {java.io.File} as a base for storing the tokens.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileCredentialStore implements CredentialStore {

	/** Default credential store file. */
	public static final String DEFAULT_STORE_FILE = "credentials.dat";
	/** Default length of the identifiers in the credential store. */
	public static final int DEFAULT_ID_LENGTH = 8;
	/** Minimal length of the identifiers in the credential store. */
	public static final int MIN_ID_LENGTH = 4;
	/** Default serialization to use. */
	public static final FileSerializationType DEFAULT_SERIALIZATION = FileSerializationType.OBJECT;

	/** Number of tries for generating a unique identifier for the store. */
	protected static final int RETRY_COUNT = 100;

	/** Length of the identifiers in the credential store. */
	protected int idLength;
	/** Serialization to use. */
	protected FileSerializationType serialization;
	/** File used as the credential store. */
	protected File credentialFile;
	/** Map of all tokens stored in the store. */
	protected Map<String, OAuth2Token> tokens;

	/** Random number generator for generating identifiers. */
	private final Random rnd;

	/**
	 * Ctor with supplied {@link java.io.File} of the credential store.
	 * @param file Credential store file.
	 */
	public FileCredentialStore(File file) {
		this(file, DEFAULT_SERIALIZATION);
	}

	/**
	 * Ctor with supplied {@link java.io.File} and {@link cz.zcu.kiv.multicloud.utils.FileSerializationType} of the credential store.
	 * @param file Credential store file.
	 * @param fileSerialization File serialization.
	 */
	public FileCredentialStore(File file, FileSerializationType fileSerialization) {
		idLength = DEFAULT_ID_LENGTH;
		serialization = fileSerialization;
		credentialFile = file;
		tokens = new HashMap<>();
		rnd = new Random();
		load();
	}

	/**
	 * Ctor with supplied path to the credential store file.
	 * @param file Path to the file.
	 */
	public FileCredentialStore(String file) {
		this(new File(file), DEFAULT_SERIALIZATION);
	}

	/**
	 * Ctor with supplied path to the credential store file and {@link cz.zcu.kiv.multicloud.utils.FileSerializationType} of the credential store.
	 * @param file Path to the file.
	 * @param fileSerialization File serialization.
	 */
	public FileCredentialStore(String file, FileSerializationType fileSerialization) {
		this(new File(file), fileSerialization);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteCredential(String identifier) {
		tokens.remove(identifier);
		save();
	}

	/**
	 * Generates a new unique random identifier for storing a token in the store.
	 * Number of tries to generate a unique identifier is limited. If the limit is reached, empty string is returned.
	 * @return Random identifier.
	 */
	private String generateRandomId() {
		StringBuilder sb = new StringBuilder();
		int retry = 0;
		do {
			while (sb.length() < idLength) {
				char ch = (char) rnd.nextInt();
				if (Utils.isUriLetterOrDigit(ch)) {
					sb.append(ch);
				}
			}
			if (tokens.containsKey(sb.toString())) {
				sb.delete(0, idLength);
			}
			retry++;
		} while (sb.length() == 0 || retry > RETRY_COUNT);
		return sb.toString();
	}

	/**
	 * Returns the file used as a credential store.
	 * @return File used.
	 */
	public File getCredentialFile() {
		return credentialFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getIdentifiers() {
		return tokens.keySet();
	}

	/**
	 * Returns the length of newly generated identifiers.
	 * @return Length of identifiers.
	 */
	public int getIdLength() {
		return idLength;
	}

	/**
	 * Returns the file serialization used.
	 * @return File serialization.
	 */
	public FileSerializationType getSerialization() {
		return serialization;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<OAuth2Token> getTokens() {
		return tokens.values();
	}

	/**
	 * Loads the file with credentials.
	 */
	@SuppressWarnings("unchecked")
	protected void load() {
		if (!credentialFile.exists()) {
			return;
		}
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(credentialFile);
			switch (serialization) {
			case OBJECT:
				ois = new ObjectInputStream(fis);
				tokens = (Map<String, OAuth2Token>) ois.readObject();
				break;
			case JSON:
				ObjectMapper mapper = Json.getInstance().getMapper();
				tokens = mapper.readValue(fis, new TypeReference<Map<String, OAuth2Token>>() {});
				break;
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null) {
					ois.close();
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
	 * {@inheritDoc}
	 */
	@Override
	public Set<Entry<String, OAuth2Token>> retrieveAllCredentials() {
		return tokens.entrySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OAuth2Token retrieveCredential(String identifier) {
		return tokens.get(identifier);
	}

	/**
	 * Saves the file with credentials.
	 */
	protected void save() {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(credentialFile);
			switch (serialization) {
			case OBJECT:
				oos = new ObjectOutputStream(fos);
				oos.writeObject(tokens);
				break;
			case JSON:
				ObjectMapper mapper = Json.getInstance().getMapper();
				mapper.writeValue(fos, tokens);
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e) {
				/* ignore closing exception */
			}
		}
	}

	/**
	 * Sets the file to use as a credential store.
	 * @param credentialFile File to use.
	 */
	public void setCredentialFile(File credentialFile) {
		this.credentialFile = credentialFile;
		load();
	}

	/**
	 * Sets the length of newly generated identifiers.
	 * @param idLength Length of identifiers.
	 */
	public void setIdLength(int idLength) {
		if (idLength >= MIN_ID_LENGTH) {
			this.idLength = idLength;
		}
	}

	/**
	 * Sets the file serialization used.
	 * @param serialization File serialization.
	 */
	public void setSerialization(FileSerializationType serialization) {
		this.serialization = serialization;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String storeCredential(OAuth2Token token) {
		String identifier = generateRandomId();
		tokens.put(identifier, token);
		save();
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String storeCredential(String identifier, OAuth2Token token) {
		String valid = identifier;
		if (identifier == null || identifier.length() < MIN_ID_LENGTH) {
			valid = generateRandomId();
		}
		tokens.put(valid, token);
		save();
		return valid;
	}

}
