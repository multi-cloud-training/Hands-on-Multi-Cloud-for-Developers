package cz.zcu.kiv.multicloud.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.json.AccountSettings;
import cz.zcu.kiv.multicloud.json.Json;

/**
 * cz.zcu.kiv.multicloud.utils/FileAccountManager.java			<br /><br />
 *
 * Class for managing user accounts for different cloud storage services. It uses files as a storage medium.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileAccountManager implements AccountManager {

	/** Default file to save user account settings. */
	public static final String DEFAULT_FILE = "accounts.json";

	/** Instance of this class. */
	private static FileAccountManager instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static FileAccountManager getInstance() {
		if (instance == null) {
			instance = new FileAccountManager();
		}
		return instance;
	}

	/** File to save the user settings to. */
	protected File settingsFile;

	/** Map of all {@link cz.zcu.kiv.multicloud.json.AccountSettings} loaded. */
	private Map<String, AccountSettings> accounts;
	/** Instance of the Jackson JSON components. */
	private final Json json;

	/**
	 * Private ctor.
	 */
	private FileAccountManager() {
		accounts = new HashMap<>();
		json = Json.getInstance();
		settingsFile = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAccountSettings(AccountSettings settings) {
		if (Utils.isNullOrEmpty(settings.getAccountId())) {
			return;
		}
		accounts.put(settings.getAccountId(), settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountSettings getAccountSettings(String accountId) {
		return accounts.get(accountId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<AccountSettings> getAllAccountSettings() {
		return accounts.values();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getIdentifiers() {
		return accounts.keySet();
	}

	/**
	 * Returns the {@link java.io.File} where the user account settings is saved.
	 * @return Account settings file.
	 */
	public File getSettingsFile() {
		return settingsFile;
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.AccountSettings} from the default file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadAccountSettings() throws IOException {
		if (settingsFile != null) {
			loadAccountSettings(settingsFile);
		} else {
			loadAccountSettings(new File(DEFAULT_FILE));
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.AccountSettings} from specified file.
	 * @param file Account settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadAccountSettings(File file) throws IOException {
		if (file.exists() && !file.isFile()) {
			throw new FileNotFoundException("Destination is not a file.");
		} else {
			settingsFile = file;
			ObjectMapper om = json.getMapper();
			accounts = om.readValue(file, new TypeReference<HashMap<String, AccountSettings>>() {});
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.AccountSettings} from specified path.
	 * @param file Path to user account settings file.
	 * @throws IOException If the file cannot be loaded.
	 */
	public void loadAccountSettings(String file) throws IOException {
		loadAccountSettings(new File(file));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeAccountSettings(String accountId) {
		accounts.remove(accountId);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.AccountSettings} to the default file.
	 */
	@Override
	public void saveAccountSettings() {
		try {
			if (settingsFile != null) {
				saveAccountSettings(settingsFile);
			} else {
				saveAccountSettings(new File(DEFAULT_FILE));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.AccountSettings} to specified file.
	 * @param file User account settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveAccountSettings(File file) throws IOException {
		settingsFile = file;
		ObjectMapper om = json.getMapper();
		om.writerWithDefaultPrettyPrinter().writeValue(file, accounts);
	}

	/**
	 * Saves {@link cz.zcu.kiv.multicloud.json.AccountSettings} to specified path.
	 * @param file Path to user account settings file.
	 * @throws IOException If the file cannot be saved.
	 */
	public void saveAccountSettings(String file) throws IOException {
		saveAccountSettings(new File(file));
	}

	/**
	 * Sets the {@link java.io.File} where the user settings is saved.
	 * @param settingsFile User settings file.
	 */
	public void setSettingsFile(File settingsFile) {
		this.settingsFile = settingsFile;
	}

}
