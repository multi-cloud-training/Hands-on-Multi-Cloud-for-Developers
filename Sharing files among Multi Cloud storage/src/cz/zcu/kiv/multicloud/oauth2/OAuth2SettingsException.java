package cz.zcu.kiv.multicloud.oauth2;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2SettingsException.java			<br /><br />
 *
 * Exception thrown when inappropriate settings for an {@link cz.zcu.kiv.multicloud.oauth2.OAuth2Grant} are supplied.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OAuth2SettingsException extends Exception {

	/** Serialization constant. */
	private static final long serialVersionUID = -3296337784219674544L;

	/**
	 * Empty ctor.
	 */
	public OAuth2SettingsException() {
		super();
	}

	/**
	 * Ctor with specified message.
	 * @param message Message.
	 */
	public OAuth2SettingsException(String message) {
		super(message);
	}

	/**
	 * Ctor with specified message and cause of the exception.
	 * @param message Message.
	 * @param cause Cause of the exception.
	 */
	public OAuth2SettingsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Ctor with specified cause of the exception.
	 * @param cause Cause of the exception.
	 */
	public OAuth2SettingsException(Throwable cause) {
		super(cause);
	}

}
