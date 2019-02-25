package cz.zcu.kiv.multicloud.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.zcu.kiv.multicloud.filesystem.FileType;

/**
 * cz.zcu.kiv.multicloud.json/FileInfo.java			<br /><br />
 *
 * Bean for holding information about a file or folder.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileInfo {

	/** Path separator string. */
	public static final String PATH_SEPARATOR = "/";
	/** Known and recognized folder mime types. */
	public static final String[] FOLDER_MIME_TYPES = {
		"text/directory",
		"inode/directory",
		"x-directory/normal",
		"resource/folder",
		"application/vnd.google-apps.folder"
	};

	/** Resource identifier. */
	private String id;
	/** Resource name. */
	private String name;
	/** Resource path. */
	private String path;
	/** Resource parents. */
	private List<ParentInfo> parents;
	/** Type of resource. */
	private FileType fileType;
	/** Mime type of the resource. */
	private String mimeType;
	/** URL for downloading the file. */
	private String downloadUrl;
	/** Size in bytes of the resource. */
	private long size;
	/** Content of a folder. */
	private List<FileInfo> content;
	/** MD5 checksum of the file. */
	private String checksum;
	/** Creation date. */
	private Date created;
	/** Last modification date. */
	private Date modified;
	/** If the resource is deleted. */
	private boolean deleted;
	/** If the resource is shared. */
	private boolean shared;
	/** If the resource is root folder. */
	private boolean isRoot;

	/**
	 * Create a copy of the file information.
	 * @return Copy of the file information.
	 */
	public FileInfo copy() {
		FileInfo obj = new FileInfo();
		if (checksum != null) {
			obj.setChecksum(new String(checksum));
		}
		obj.setContent(new ArrayList<FileInfo>());
		obj.getContent().addAll(content);
		if (created != null) {
			obj.setCreated(new Date(created.getTime()));
		}
		obj.setDeleted(deleted);
		if (downloadUrl != null) {
			obj.setDownloadUrl(new String(downloadUrl));
		}
		obj.setFileType(fileType);
		if (id != null) {
			obj.setId(new String(id));
		}
		obj.setIsRoot(isRoot);
		if (mimeType != null) {
			obj.setMimeType(new String(mimeType));
		}
		if (modified != null) {
			obj.setModified(new Date(modified.getTime()));
		}
		if (name != null) {
			obj.setName(new String(name));
		}
		obj.setParents(new ArrayList<ParentInfo>());
		obj.getParents().addAll(parents);
		if (path != null) {
			obj.setPath(new String(path));
		}
		obj.setShared(shared);
		obj.setSize(size);
		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FileInfo)) {
			return false;
		}
		FileInfo cmp = (FileInfo) obj;
		return (cmp.getName().equals(name) && cmp.getSize() == size && cmp.getFileType() == fileType);
	}

	/**
	 * Method for filling missing parameters based on already supplied ones.
	 */
	public void fillMissing() {
		if (parents == null) {
			parents = new ArrayList<>();
		}
		if (content == null) {
			content = new ArrayList<>();
		}
		if (path != null) {
			if (name == null && path.contains(PATH_SEPARATOR)) {
				name = path.substring(path.lastIndexOf(PATH_SEPARATOR) + 1);
			}
			if (parents.isEmpty()) {
				if (path.contains(PATH_SEPARATOR)) {
					ParentInfo parent = new ParentInfo();
					parent.setPath(path.substring(0, path.lastIndexOf(PATH_SEPARATOR) + 1));
					if (parent.getPath().equals(PATH_SEPARATOR)) {
						parent.setIsRoot(true);
					}
					parents.add(parent);
				}
			}
		}
		if (fileType == null) {
			if (mimeType != null) {
				for (String folderType: FOLDER_MIME_TYPES)  {
					if (mimeType.equalsIgnoreCase(folderType)) {
						fileType = FileType.FOLDER;
						break;
					}
				}
				if (fileType == null) {
					fileType = FileType.FILE;
				}
			} else {
				fileType = FileType.FOLDER;
			}
		}
	}

	/**
	 * Returns the content of a folder.
	 * @return Folder content.
	 */
	public List<FileInfo> getContent() {
		return content;
	}

	/**
	 * Returns the creation date.
	 * @return Creation date.
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * Returns URL for downloading the file.
	 * @return URL for downloading the file.
	 */
	public String getDownloadUrl() {
		return downloadUrl;
	}

	/**
	 * Returns the type of the resource.
	 * @return Resource type.
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * Returns the MD5 checksum of the file.
	 * @return MD5 checksum of the file.
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Returns the identifier of the resource.
	 * @return Resource identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the mime type of the resource.
	 * @return Resouce mime type.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Returns the last modification date.
	 * @return Last modification date.
	 */
	public Date getModified() {
		return modified;
	}

	/**
	 * Returns the name of the resource.
	 * @return Resource name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the parents of the resource.
	 * @return Resource parents.
	 */
	public List<ParentInfo> getParents() {
		return parents;
	}

	/**
	 * Returns the path of the resource.
	 * @return Resource path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the size of the resource.
	 * @return Size of the resource.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Returns if the resource is deleted.
	 * @return If the resource is deleted.
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Returns if the resource is root folder.
	 * @return If the resource is root folder.
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * Returns if the resource is shared.
	 * @return If the resource is shared.
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * Sets the content of a folder.
	 * @param content Content of a folder.
	 */
	public void setContent(List<FileInfo> content) {
		this.content = content;
	}

	/**
	 * Sets the creation date.
	 * @param created Creation date.
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * Sets if the resource is deleted.
	 * @param deleted If the resource is deleted.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * Sets the URL for downloading the file.
	 * @param downloadUrl URL for downloading the file.
	 */
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	/**
	 * Sets the type of the resource.
	 * @param fileType Resource type.
	 */
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	/**
	 * Sets the MD5 checksum of the file.
	 * @param checksum MD5 checksum of the file.
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * Sets the identifier of the resource.
	 * @param id Resource identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets if the resource is root folder.
	 * @param isRoot If the resource is root folder.
	 */
	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * Sets the mime type of the resource.
	 * @param mimeType Resource mime type.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Sets the last modification date.
	 * @param modified Last modification date.
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

	/**
	 * Sets the name of the resource.
	 * @param name Resource name.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the parent of the resource.
	 * @param parent Resource parent.
	 */
	public void setParent(String parent) {
		if (parents == null) {
			parents = new ArrayList<>();
		}
		ParentInfo p = new ParentInfo();
		p.setId(parent);
		parents.add(p);
	}

	/**
	 * Sets the parents of the resource.
	 * @param parents Resource parents.
	 */
	public void setParents(List<ParentInfo> parents) {
		this.parents = parents;
	}

	/**
	 * Sets the path of the resource.
	 * @param path Resource path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets if the resource is shared.
	 * @param shared If the resource is shared.
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	/**
	 * Sets the size of the resource.
	 * @param size Size of the resource.
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Sets the type of the resource from a string.
	 * @param fileType Resource type string.
	 */
	public void setStringFileType(String fileType) {
		this.fileType = FileType.fromString(fileType);
	}

}
