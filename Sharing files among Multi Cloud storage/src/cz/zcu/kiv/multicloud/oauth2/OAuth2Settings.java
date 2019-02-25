package cz.zcu.kiv.multicloud.oauth2;

import java.util.HashMap;
import java.util.Map;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2Settings.java			<br /><br />
 *
 * Generic settings for the OAuth 2.0 implementation.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OAuth2Settings {

	/** Grant type being used. */
	private OAuth2GrantType grantType;
	/** Implementation class of the extension grant. */
	private Class<? extends OAuth2Grant> grantClass;

	/** Client identifier. */
	private String clientId;
	/** Client secret. */
	private String clientSecret;
	/** URI of the authorization server. */
	private String authorizeUri;
	/** URI of the redirect server. */
	private String redirectUri;
	/** URI of the token server. */
	private String tokenUri;
	/** Requested scopes. */
	private String scope;
	/** Username for the authorization. */
	private String username;
	/** Password for the authorization. */
	private String password;
	/** Refresh token for requesting new access token. */
	private String refreshToken;
	/** Additional parameters passed to the authorization server. */
	private final Map<String, String> extraAuthorizeParams;
	/** Additional parameters passed to the token server. */
	private final Map<String, String> extraTokenParams;

	/**
	 * Ctor.
	 */
	public OAuth2Settings() {
		extraAuthorizeParams = new HashMap<>();
		extraTokenParams = new HashMap<>();
	}

	/**
	 * Adds one additional parameter for the authorization server.
	 * @param key Parameter name.
	 * @param value Parameter value.
	 */
	public void addExtraAuthorizeParams(String key, String value) {
		extraAuthorizeParams.put(key, value);
	}

	/**
	 * Adds one additional parameter for the token server.
	 * @param key Parameter name.
	 * @param value Parameter value.
	 */
	public void addExtraTokenParams(String key, String value) {
		extraTokenParams.put(key, value);
	}

	/**
	 * Returns the URI of the authorization server.
	 * @return Authorization server URI.
	 */
	public String getAuthorizeUri() {
		return authorizeUri;
	}

	/**
	 * Returns the client identifier.
	 * @return Client identifier.
	 */
	public String getClientId() {
		return clientId;
	}

	/**
	 * Returns the client secret.
	 * @return Client secret.
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * Return all the additional parameters for the authorization server.
	 * @return Additional parameters.
	 */
	public Map<String, String> getExtraAuthorizeParams() {
		return extraAuthorizeParams;
	}

	/**
	 * Returns all the additional parameters for the token server.
	 * @return Additional parameters.
	 */
	public Map<String, String> getExtraTokenParams() {
		return extraTokenParams;
	}

	/**
	 * Returns the class of the extention grant.
	 * @return Extension grant class.
	 */
	public Class<? extends OAuth2Grant> getGrantClass() {
		return grantClass;
	}

	/**
	 * Returns the type of grant.
	 * @return Grant type.
	 */
	public OAuth2GrantType getGrantType() {
		return grantType;
	}

	/**
	 * Returns the password for the authorization.
	 * @return Password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the URI of redirect server.
	 * @return Redirect server URI.
	 */
	public String getRedirectUri() {
		return redirectUri;
	}

	/**
	 * Returns the refresh token for requesting new access token.
	 * @return Refresh token.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * Returns the scopes requested.
	 * @return Scopes.
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Returns the URI of token server.
	 * @return Token server URI.
	 */
	public String getTokenUri() {
		return tokenUri;
	}

	/**
	 * Returns  the username for the authorization.
	 * @return Username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the URI of the authorization server.
	 * @param authorizeUri Authorization server URI.
	 */
	public void setAuthorizeUri(String authorizeUri) {
		this.authorizeUri = authorizeUri;
	}

	/**
	 * Sets the client identifier.
	 * @param clientId Client identifier.
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	/**
	 * Sets the client secret.
	 * @param clientSecret Client secret.
	 */
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * Sets the class of the extension grant.
	 * @param grantClass Extension grant class.
	 */
	public void setGrantClass(Class<? extends OAuth2Grant> grantClass) {
		this.grantClass = grantClass;
	}

	/**
	 * Sets the class of the extension grant.
	 * @param grantClass Name of the extension grant class.
	 * @throws OAuth2SettingsException If the extension grant class was not found.
	 */
	@SuppressWarnings("unchecked")
	public void setGrantClass(String grantClass) throws OAuth2SettingsException {
		try {
			this.grantClass = (Class<? extends OAuth2Grant>) Class.forName(grantClass);
		} catch (ClassNotFoundException e) {
			throw new OAuth2SettingsException("Extension grant class not found.");
		}
	}

	/**
	 * Sets the type of grant.
	 * @param grantType Grant type.
	 */
	public void setGrantType(OAuth2GrantType grantType) {
		this.grantType = grantType;
	}

	/**
	 * Sets the password for the authorization.
	 * @param password Password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Sets the URI of the redirect server.
	 * @param redirectUri Redirect server URI.
	 */
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	/**
	 * Sets the refresh token to specified value.
	 * @param refreshToken Refresh token.
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * Sets the scopes to be requested.
	 * @param scope Requested scopes.
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * Sets the URI of the token server.
	 * @param tokenUri Token server URI.
	 */
	public void setTokenUri(String tokenUri) {
		this.tokenUri = tokenUri;
	}

	/**
	 * Sets the username for the authorization.
	 * @param username Username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
