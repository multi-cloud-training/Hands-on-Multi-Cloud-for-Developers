package cz.zcu.kiv.multicloud.json;

/**
 * cz.zcu.kiv.multicloud.json/ParentInfo.java			<br /><br />
 *
 * Bean for holding the basic information about file or folder parent.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ParentInfo {

	/** Identifier of the parent. */
	private String id;
	/** Path to the parent. */
	private String path;
	/** If the parent is root folder. */
	private boolean isRoot;

	/**
	 * Returns the identifier of the parent.
	 * @return Parent identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the path to the parent.
	 * @return Parent path.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns true if the parent is root folder.
	 * @return If the parent is root folder.
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * Sets the identifier of the parent.
	 * @param id Parent identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets if the parent is root folder.
	 * @param isRoot If the parent is root folder.
	 */
	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * Sets the path to the parent.
	 * @param path Parent path.
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
