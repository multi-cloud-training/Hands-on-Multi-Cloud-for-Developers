package cz.zcu.kiv.multicloud;

/**
 * cz.zcu.kiv.multicloud/MultiCloudException.java			<br /><br />
 *
 * Exception thrown when a problem occurs in the library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloudException extends Exception {

	/** Serialization constant. */
	private static final long serialVersionUID = 5802588960809459596L;

	/**
	 * Empty ctor.
	 */
	public MultiCloudException() {
		super();
	}

	/**
	 * Ctor with specified message.
	 * @param message Message.
	 */
	public MultiCloudException(String message) {
		super(message);
	}

	/**
	 * Ctor with specified message and cause of the exception.
	 * @param message Message.
	 * @param cause Cause of the exception.
	 */
	public MultiCloudException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Ctor with specified cause of the exception.
	 * @param cause Cause of the exception.
	 */
	public MultiCloudException(Throwable cause) {
		super(cause);
	}

}
