package cz.zcu.kiv.multicloud.json;

/**
 * cz.zcu.kiv.multicloud.json/AccountInfo.java			<br /><br />
 *
 * Bean for storing information about the user of the user account.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountInfo {

	/** User identifier. */
	private String id;
	/** User name. */
	private String name;

	/**
	 * Returns the identifier of the user.
	 * @return User identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name of the user.
	 * @return User name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the identifier of the user.
	 * @param id User identifier.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Sets the name of the user.
	 * @param name User name.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
