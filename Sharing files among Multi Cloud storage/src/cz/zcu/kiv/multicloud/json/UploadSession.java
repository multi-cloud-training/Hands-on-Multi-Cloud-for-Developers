package cz.zcu.kiv.multicloud.json;

/**
 * cz.zcu.kiv.multicloud.json/UploadSession.java			<br /><br />
 *
 * Bean for holding information about chunked upload session.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class UploadSession {

	/** Identifier of the session. */
	private String session;
	/** Offset of the data. */
	private long offset;

	/**
	 * Returns the data offset.
	 * @return Data offset.
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Returns the identifier of the session.
	 * @return Session identifier.
	 */
	public String getSession() {
		return session;
	}

	/**
	 * Sets the data offset.
	 * @param offset Data offset.
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Sets the identifier of the session.
	 * @param session Session identifier.
	 */
	public void setSession(String session) {
		this.session = session;
	}

}
