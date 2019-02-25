package cz.zcu.kiv.multicloud.json;

import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * cz.zcu.kiv.multicloud.json/OperationError.java			<br /><br />
 *
 * Bean for holding error message that occurred during {@link cz.zcu.kiv.multicloud.filesystem.Operation}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OperationError {

	/** Status code of the HTTP response. */
	private int code;
	/** Message of the error. */
	private String message;

	/**
	 * Ctor.
	 */
	public OperationError() {
		code = -1;
		message = null;
	}

	/**
	 * Returns the status code of the HTTP response.
	 * @return Status code.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns the message of the error.
	 * @return Error message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the status code of the HTTP response.
	 * @param code Status code.
	 */
	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * Sets the status code of HTTP response from string. Needed for Jackson JSON parser.
	 * @param code Status code.
	 */
	@JsonSetter
	public void setCode(String code) {
		try {
			this.code = Integer.parseInt(code);
		} catch (Exception e) {
			this.code = -1;
		}
	}

	/**
	 * Sets the message of the error.
	 * @param message Error message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Error: " + code + " - " + message;
	}

}
