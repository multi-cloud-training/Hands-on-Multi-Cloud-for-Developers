package cz.zcu.kiv.multicloud.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * cz.zcu.kiv.multicloud.json/AccountSettings.java			<br /><br />
 *
 * Bean for holding information about user account registered by a cloud storage service provider. No user credentials are stored here.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AccountSettings {

	/** User account identifier. */
	@JsonProperty("account_id")
	private String accountId;

	/** Settings identifier. */
	@JsonProperty("settings_id")
	private String settingsId;
	/** Token identifier. */
	@JsonProperty("token_id")
	private String tokenId;

	/**
	 * Returns the identifier of the user account.
	 * @return User account identifier.
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * Returns the identifier of the settings.
	 * @return Settings identifier.
	 */
	public String getSettingsId() {
		return settingsId;
	}

	/**
	 * Returns the identifier of the token.
	 * @return Token identifier.
	 */
	public String getTokenId() {
		return tokenId;
	}

	/**
	 * Determines if the user account has authorized this client.
	 * @return If the client is authorized.
	 */
	@JsonIgnore
	public boolean isAuthorized() {
		return (tokenId != null);
	}

	/**
	 * Sets the identifier of the settings.
	 * @param settingsId Settings identifier.
	 */
	public void setSettingsId(String settingsId) {
		this.settingsId = settingsId;
	}

	/**
	 * Sets the identifier of the token.
	 * @param tokenId Token identifier.
	 */
	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	/**
	 * Sets the identifier of the user account.
	 * @param accountId User account identifier.
	 */
	public void setUserId(String accountId) {
		this.accountId = accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return accountId;
	}

}
