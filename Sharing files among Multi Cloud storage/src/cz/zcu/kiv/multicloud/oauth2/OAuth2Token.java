package cz.zcu.kiv.multicloud.oauth2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * cz.zcu.kiv.multicloud.oauth2/OAuth2Token.java			<br /><br />
 *
 * Generic OAuth 2.0 token class.
 * It is able to hold Bearer token as described in <a href="http://tools.ietf.org/html/rfc6749">OAuth 2.0 specification</a>.
 * It is also capable of storing MAC token as described in current draft of the <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-http-mac-05">OAuth 2.0 MAC Tokens</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class OAuth2Token implements Serializable {

	/** Serialization constant. */
	private static final long serialVersionUID = 2872874560545563982L;

	/** Timestamp in seconds representing token creation time. */
	protected long timestamp;

	/** Type of token. */
	private OAuth2TokenType type;

	/** Access token. */
	private String accessToken;
	/** Refresh token. */
	private String refreshToken;
	/** Key identifier. */
	private String keyId;
	/** Session key. */
	private String macKey;
	/** MAC algorithm. */
	private String macAlgorithm;
	/** Expiration time in seconds. */
	private int expiresIn;

	/**
	 * Ctor.
	 */
	public OAuth2Token() {
		type = OAuth2TokenType.EMPTY;
		accessToken = null;
		refreshToken = null;
		keyId = null;
		macKey = null;
		macAlgorithm = null;
		expiresIn = -1;
	}

	/**
	 * Returns the access token.
	 * @return Access token.
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * Returns the number of seconds the access token expires in.
	 * @return Number of seconds.
	 */
	public int getExpiresIn() {
		return expiresIn;
	}

	/**
	 * Returns the key identifier for MAC token type.
	 * @return Key identifier.
	 */
	public String getKeyId() {
		return keyId;
	}

	/**
	 * Returns the algorithm used in MAC token.
	 * @return MAC algorithm.
	 */
	public String getMacAlgorithm() {
		return macAlgorithm;
	}

	/**
	 * Returns the session key for MAC token.
	 * @return Session key.
	 */
	public String getMacKey() {
		return macKey;
	}

	/**
	 * Returns the refresh token.
	 * @return Refresh token.
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * Returns the time when the access token was created.
	 * @return Timestamp of the creation of the token.
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the type of this token.
	 * @return Type of token.
	 */
	public OAuth2TokenType getType() {
		return type;
	}

	/**
	 * Determines if the token has already expired.
	 * @return If the token expired.
	 */
	@JsonIgnore
	public boolean isExpired() {
		if (expiresIn >= 0) {
			return ((System.currentTimeMillis() / 1000) >= timestamp + expiresIn);
		} else {
			return false;
		}
	}

	/**
	 * Stores the access token.
	 * @param accessToken Access token.
	 */
	public void setAccessToken(String accessToken) {
		this.timestamp = System.currentTimeMillis() / 1000;
		this.accessToken = accessToken;
	}

	/**
	 * Sets the expiration time in seconds.
	 * @param expiresIn Expiration time.
	 */
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	/**
	 * Sets the key identifier.
	 * @param keyId Key identifier.
	 */
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	/**
	 * Sets the MAC algorithm used.
	 * @param macAlgorithm MAC algorithm.
	 */
	public void setMacAlgorithm(String macAlgorithm) {
		this.macAlgorithm = macAlgorithm;
	}

	/**
	 * Sets the session key for MAC token.
	 * @param macKey Session key.
	 */
	public void setMacKey(String macKey) {
		this.macKey = macKey;
	}

	/**
	 * Stores the refresh token.
	 * @param refreshToken Refresh token.
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * Sets the time in seconds of the creation of the token.
	 * @param timestamp Time in seconds.
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sets the type of token stored.
	 * @param type Type of token.
	 */
	public void setType(OAuth2TokenType type) {
		this.type = type;
	}

	/**
	 * Converts the token to string representation for use in HTTP Authorization header.
	 * @return Header string.
	 */
	public String toHeaderString() {
		StringBuilder sb = new StringBuilder();
		switch (type) {
		case BEARER:
			sb.append("Bearer ");
			sb.append(accessToken);
			break;
		case MAC:
			/* header representation not yet implemented */
			break;
		default:
			break;
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OAuth 2.0 Token:");
		sb.append("\n  type: ");
		sb.append(type.toString());
		if (accessToken != null) {
			sb.append("\n  access token: ");
			sb.append(accessToken);
		}
		if (refreshToken != null) {
			sb.append("\n  refresh token: ");
			sb.append(refreshToken);
		}
		if (keyId != null) {
			sb.append("\n  kid: ");
			sb.append(keyId);
		}
		if (macKey != null) {
			sb.append("\n  mac key: ");
			sb.append(macKey);
		}
		if (macAlgorithm != null) {
			sb.append("\n  mac algorithm: ");
			sb.append(macAlgorithm);
		}
		if (expiresIn != -1) {
			sb.append("\n  expires in: ");
			sb.append(expiresIn);
		}
		return sb.toString();
	}

}
