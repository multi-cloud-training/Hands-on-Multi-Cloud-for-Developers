package cz.zcu.kiv.multicloud.filesystem;

import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileCloudSource.java			<br /><br />
 *
 * Class for holding file information, access token and the settings for downloading it from or uploading it to a cloud storage service.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileCloudSource {

	/** Account name. */
	private String accountName;
	/** File information. */
	private FileInfo file;
	/** Remote file to be updated. */
	private FileInfo remote;
	/** Name of the file. */
	private String fileName;
	/** Cloud begin request settings. */
	private CloudRequest beginRequest;
	/** Cloud execute request settings. */
	private CloudRequest execRequest;
	/** Cloud finish request settings. */
	private CloudRequest finishRequest;
	/** Access token for the cloud storage service. */
	private OAuth2Token token;

	/**
	 * Empty ctor.
	 */
	public FileCloudSource() {
		accountName = null;
		file = null;
		remote = null;
		fileName = null;
		beginRequest = null;
		execRequest = null;
		finishRequest = null;
		token = null;
	}

	/**
	 * Ctor with file information, access token and cloud request settings supplied.
	 * @param accountName Account name.
	 * @param file File information.
	 * @param remote Remote file to be updated.
	 * @param fileName Name of the file.
	 * @param beginRequest Cloud begin request settings.
	 * @param execRequest Cloud execute request settings.
	 * @param finishRequest Cloud finish request settings.
	 * @param token Access token for the cloud storage service.
	 */
	public FileCloudSource(String accountName, FileInfo file, FileInfo remote, String fileName, CloudRequest beginRequest, CloudRequest execRequest, CloudRequest finishRequest, OAuth2Token token) {
		this.accountName = accountName;
		this.file = file;
		this.remote = remote;
		this.fileName = fileName;
		this.beginRequest = beginRequest;
		this.execRequest = execRequest;
		this.finishRequest = finishRequest;
		this.token = token;
	}

	/**
	 * Returns the account name.
	 * @return Account name.
	 */
	public String getAccountName() {
		return accountName;
	}

	/**
	 * Returns cloud begin request settings.
	 * @return Cloud begin request settings.
	 */
	public CloudRequest getBeginRequest() {
		return beginRequest;
	}

	/**
	 * Returns the cloud execute request settings.
	 * @return Cloud execute request settings.
	 */
	public CloudRequest getExecRequest() {
		return execRequest;
	}

	/**
	 * Returns the file information.
	 * @return File information.
	 */
	public FileInfo getFile() {
		return file;
	}

	/**
	 * Returns the name of the file.
	 * @return Name of the file.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Returns cloud finish request settings.
	 * @return Cloud finish request settings.
	 */
	public CloudRequest getFinishRequest() {
		return finishRequest;
	}

	/**
	 * Return the remote file to be updated.
	 * @return Remote file to be updated.
	 */
	public FileInfo getRemote() {
		return remote;
	}

	/**
	 * Returns the access token for the storage service.
	 * @return Access token for the storage service.
	 */
	public OAuth2Token getToken() {
		return token;
	}

	/**
	 * Sets the account name.
	 * @param accountName Account name.
	 */
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	/**
	 * Sets cloud begin request settings.
	 * @param beginRequest Cloud begin request settings.
	 */
	public void setBeginRequest(CloudRequest beginRequest) {
		this.beginRequest = beginRequest;
	}

	/**
	 * Sets the cloud execute request settings.
	 * @param execRequest Cloud execute request settings.
	 */
	public void setExecRequest(CloudRequest execRequest) {
		this.execRequest = execRequest;
	}

	/**
	 * Sets the file information.
	 * @param file File information.
	 */
	public void setFile(FileInfo file) {
		this.file = file;
	}

	/**
	 * Sets the name of the file.
	 * @param fileName Name of the file.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Sets cloud finish request settings.
	 * @param finishRequest Cloud finish request settings.
	 */
	public void setFinishRequest(CloudRequest finishRequest) {
		this.finishRequest = finishRequest;
	}

	/**
	 * Sets the remote file to be updated.
	 * @param remote Remote file to be updated.
	 */
	public void setRemote(FileInfo remote) {
		this.remote = remote;
	}

	/**
	 * Sets the access token for the storage service.
	 * @param token Access token for the storage service.
	 */
	public void setToken(OAuth2Token token) {
		this.token = token;
	}

}
