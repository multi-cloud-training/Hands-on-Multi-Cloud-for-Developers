package cz.zcu.kiv.multicloud.filesystem;

/**
 * cz.zcu.kiv.multicloud.filesystem/OperationType.java			<br /><br />
 *
 * List of all operation types enabled by the library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum OperationType {

	ACCOUNT_INFO,
	ACCOUNT_QUOTA,
	FILE_DOWNLOAD,
	FILE_UPLOAD,
	FOLDER_CREATE,
	FOLDER_LIST,
	RENAME,					// applies to files and folders
	COPY,					// applies to files and folders
	MOVE,					// applies to files and folders
	DELETE,					// applies to files and folders
	SEARCH,
	METADATA

}
