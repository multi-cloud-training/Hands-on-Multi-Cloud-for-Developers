package cz.zcu.kiv.multicloud;

/**
 * cz.zcu.kiv.multicloud/AbortedException.java			<br /><br />
 *
 * Exception for indication abortion of an operation.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AbortedException extends MultiCloudException {

	/** Serialization constant. */
	private static final long serialVersionUID = -6208989395291553276L;

	/**
	 * Empty ctor.
	 */
	public AbortedException() {
		super();
	}

	/**
	 * Ctor with specified message.
	 * @param message Message.
	 */
	public AbortedException(String message) {
		super(message);
	}

	/**
	 * Ctor with specified message and cause of the exception.
	 * @param message Message.
	 * @param cause Cause of the exception.
	 */
	public AbortedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Ctor with specified cause of the exception.
	 * @param cause Cause of the exception.
	 */
	public AbortedException(Throwable cause) {
		super(cause);
	}

}
