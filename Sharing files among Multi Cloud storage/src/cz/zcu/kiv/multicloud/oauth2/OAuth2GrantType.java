package cz.zcu.kiv.multicloud.oauth2;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2GrantType.java			<br /><br />
 *
 * Enumeration of all authorization grant types defined by <a href="http://tools.ietf.org/html/rfc6749#section-4">RFC 6749</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum OAuth2GrantType {

	AUTHORIZATION_CODE_GRANT,
	IMPLICIT_GRANT,
	RESOURCE_OWNER_PASSWORD_CREDENTIAL_GRANT,
	CLIENT_CREDENTIAL_GRANT,
	EXTENSION_GRANT

}
