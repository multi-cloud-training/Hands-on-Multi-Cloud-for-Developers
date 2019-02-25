package cz.zcu.kiv.multicloud.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.json.Json;

/**
 * cz.zcu.kiv.multicloud.utils/FileCloudManager.java			<br /><br />
 *
 * Class for managing settings for different cloud storage service providers.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileCloudManager implements CloudManager {

	/** Default location of cloud storage settings files. */
	public static final String DEFAULT_FOLDER = "definitions";
	/** Default file suffix for loading settings. */
	public static final String DEFAULT_FILE_SUFFIX = ".json";

	/** Instance of this class. */
	private static FileCloudManager instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static FileCloudManager getInstance() {
		if (instance == null) {
			instance = new FileCloudManager();
		}
		return instance;
	}

	/** Map of all {@link cz.zcu.kiv.multicloud.json.CloudSettings} loaded. */
	private final Map<String, CloudSettings> settings;
	/** Instance of the Jackson JSON components. */
	private final Json json;

	/**
	 * Private ctor.
	 */
	private FileCloudManager() {
		settings = new HashMap<>();
		json = Json.getInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<CloudSettings> getAllCloudSettings() {
		return settings.values();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CloudSettings getCloudSettings(String cloudName) {
		return settings.get(cloudName);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getIdentifiers() {
		return settings.keySet();
	}

	/**
	 * Load {@link cz.zcu.kiv.multicloud.json.CloudSettings} from default location.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings() throws IOException {
		loadCloudSettings(new File(DEFAULT_FOLDER));
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.CloudSettings} from specified path.
	 * If the path parameter points to a folder, all the files in the folder are loaded.
	 * @param path Path to be loaded.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings(File path) throws IOException {
		if (path.isDirectory()) {
			loadFolder(path);
		} else if (path.isFile()) {
			loadFile(path);
		}
	}

	/**
	 * Loads {@link cz.zcu.kiv.multicloud.json.CloudSettings} from specified path.
	 * If the path parameter points to a folder, all the files in the folder are loaded.
	 * @param path Path to be loaded.
	 * @throws IOException If the location doesn't exist or some files are unreadable.
	 */
	public void loadCloudSettings(String path) throws IOException {
		loadCloudSettings(new File(path));
	}

	/**
	 * Loads a single {@link cz.zcu.kiv.multicloud.json.CloudSettings} from file.
	 * @param path Path to the file.
	 * @throws IOException If the file cannot be loaded.
	 */
	private void loadFile(File path) throws IOException {
		ObjectMapper om = json.getMapper();
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		CloudSettings cs = om.readValue(path, CloudSettings.class);
		if (Utils.isNullOrEmpty(cs.getSettingsId())) {
			throw new JsonMappingException("File must contain \"name\" property.");
		}
		if (!settings.containsKey(cs.getSettingsId())) {
			settings.put(cs.getSettingsId(), cs);
		}
	}

	/**
	 * Loads all files in the supplied folder.
	 * @param path Path to the folder.
	 * @throws IOException If one or more of the files cannot be loaded.
	 */
	private void loadFolder(File path) throws IOException {
		/* filter files in the folder */
		FilenameFilter filter = new FilenameFilter() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(DEFAULT_FILE_SUFFIX);
			}
		};
		/* try to load all the files in the folder */
		List<String> failed = new ArrayList<>();
		for (File f: path.listFiles(filter)) {
			try {
				loadFile(f);
			} catch (IOException e) {
				failed.add(f.getName());
			}
		}
		/* throw an exception if some of the files cannot be loaded */
		if (!failed.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed to load one or more files:");
			for (int i = 0; i < failed.size(); i++) {
				if (i > 0) {
					sb.append(",");
				}
				sb.append(" " + failed.get(i));
			}
			throw new IOException(sb.toString());
		}
	}

}
