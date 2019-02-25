package cz.zcu.kiv.multicloud.oauth2;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2Error.java			<br /><br />
 *
 * Class for holding information about an error that occurred.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OAuth2Error {

	/** Type of error. */
	private OAuth2ErrorType type;
	/** Description of the error. */
	private String description;
	/** URI with additional information about the error. */
	private String uri;

	/**
	 * Ctor.
	 */
	public OAuth2Error() {
		type = OAuth2ErrorType.SUCCESS;
		description = null;
		uri = null;
	}

	/**
	 * Returns the description of the error occurred.
	 * @return Description of the error.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the type of error occurred.
	 * @return Type of error.
	 */
	public OAuth2ErrorType getType() {
		return type;
	}

	/**
	 * Returns the URI with additional information about the error.
	 * @return URI with additional information.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the description of the error occurred.
	 * @param description Description of the error.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Sets the type of error that occurred.
	 * @param type Type of error.
	 */
	public void setType(OAuth2ErrorType type) {
		this.type = type;
	}

	/**
	 * Sets the URI with additional information about the error occurred.
	 * @param uri URI with additional infromation.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OAuth 2.0 Error: ");
		sb.append(type.toString());
		if (description != null) {
			sb.append(" - ");
			sb.append(description);
		}
		if (uri != null) {
			sb.append("\n");
			sb.append(uri);
		}
		return sb.toString();
	}

}
