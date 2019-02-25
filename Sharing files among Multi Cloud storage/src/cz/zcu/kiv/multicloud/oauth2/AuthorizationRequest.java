package cz.zcu.kiv.multicloud.oauth2;

import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.oauth2/AuthorizationRequest.java			<br /><br />
 *
 * Represents an authorization request of the <a href="http://tools.ietf.org/html/rfc6749">OAuth 2.0 specification</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AuthorizationRequest {

	/** URI to be requested. */
	private final String requestUri;

	/**
	 * Ctor.
	 * @param requestUri URI to be requested.
	 */
	public AuthorizationRequest(String requestUri) {
		this.requestUri = requestUri;
	}

	/**
	 * Returns the URI to be requested.
	 * @return URI to be requested.
	 */
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * Determines if any additional action is required.
	 * @return If additional action is required.
	 */
	public boolean isActionRequied() {
		return !Utils.isNullOrEmpty(requestUri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (!Utils.isNullOrEmpty(requestUri)) {
			return "To authorize this application, visit:" + System.getProperty("line.separator") + requestUri;
		} else {
			return "No action required.";
		}
	}

}
