package cz.zcu.kiv.multicloud.oauth2;

import java.io.Closeable;


/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2Grant.java			<br /><br />
 *
 * Basic interface for implementing OAuth 2.0 grant.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface OAuth2Grant extends Closeable {

	/**
	 * Start of the authorization process. If the implemented grant requires a redirect to authorization server, it is returned as a {@link cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest}. If no redirect is required, null should be returned.
	 * @return Request for redirect to external web page.
	 */
	AuthorizationRequest authorize();

	/**
	 * Returns the last error that has occurred during the authorization process.
	 * @return Last error occurred.
	 */
	OAuth2Error getError();

	/**
	 * Returns the access (and refresh) token after successful authorization.
	 * @return Access token (and optional refresh token).
	 */
	OAuth2Token getToken();

	/**
	 * Setting up the grant.
	 * @param settings Settings.
	 * @throws OAuth2SettingsException Exception when not proper settings are passed.
	 */
	void setup(OAuth2Settings settings) throws OAuth2SettingsException;

}
