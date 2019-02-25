package cz.zcu.kiv.multicloud.json;

/**
 * cz.zcu.kiv.multicloud.json/AccountQuota.java			<br /><br />
 *
 * Bean for holding information about user account quota.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountQuota {

	/** Total number of bytes assigned to the user account. */
	private long totalBytes;
	/** Number of bytes used in the storage. */
	private long usedBytes;
	/** Number of bytes available in the storage. */
	private long freeBytes;

	public AccountQuota() {
		totalBytes = -1;
		usedBytes = -1;
		freeBytes = -1;
	}

	/**
	 * Fills the missing values.
	 */
	private void calculateMissing() {
		if (totalBytes > -1) {
			if (usedBytes > -1 && freeBytes == -1) {
				freeBytes = totalBytes - usedBytes;
			} else if (usedBytes == -1 && freeBytes > -1) {
				usedBytes = totalBytes - freeBytes;
			}
		}
	}

	/**
	 * Returns the number of bytes available in the storage.
	 * @return Available bytes.
	 */
	public long getFreeBytes() {
		return freeBytes;
	}

	/**
	 * Returns the total number of bytes assigned to the user account.
	 * @return Total bytes.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * Returns the number of bytes used in the storage.
	 * @return Used bytes.
	 */
	public long getUsedBytes() {
		return usedBytes;
	}

	/**
	 * Sets the number of bytes available in the storage.
	 * @param freeBytes Available bytes.
	 */
	public void setFreeBytes(long freeBytes) {
		this.freeBytes = freeBytes;
		this.usedBytes = -1;
		calculateMissing();
	}

	/**
	 * Sets the total number of bytes assigned to the user account.
	 * @param totalBytes Total bytes.
	 */
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
		calculateMissing();
	}

	/**
	 * Sets the number of bytes used in the storage.
	 * @param usedBytes Used bytes.
	 */
	public void setUsedBytes(long usedBytes) {
		this.usedBytes = usedBytes;
		this.freeBytes = -1;
		calculateMissing();
	}

}
