package cz.zcu.kiv.multicloud.oauth2;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2ErrorType.java			<br /><br />
 *
 * List of all the error types that can occur in a response from the authorization server.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum OAuth2ErrorType {

	SUCCESS,					// no error
	INVALID_REQUEST,
	UNAUTHORIZED_CLIENT,
	ACCESS_DENIED,
	UNSUPPORTED_RESPONSE_TYPE,
	INVALID_SCOPE,
	SERVER_ERROR,
	TEMPORARILY_UNAVAILABLE,
	INVALID_CLIENT,
	INVALID_GRANT,
	UNSUPPORTED_GRANT_TYPE,
	ACCESS_TOKEN_MISSING,		// custom error to reflect on missing access token
	CODE_MISSING,				// custom error to reflect on missing code parameter
	STATE_MISSING,				// custom error to reflect on missing state parameter
	STATE_MISMATCH,				// custom error to reflect on mismatch in state parameter
	TOKEN_TYPE_MISSING			// custom error to reflect on missing token type parameter

}
