package cz.zcu.kiv.multicloud.oauth2;

/**
 * cz.zcu.kiv.multicloud.oauth2/AuthorizationCallback.java			<br /><br />
 *
 * Interface for handling {@link cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest} requests.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface AuthorizationCallback {

	/**
	 * Handling of the {@link cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest} requests.
	 * @param request Authorization request.
	 */
	void onAuthorizationRequest(AuthorizationRequest request);

}
