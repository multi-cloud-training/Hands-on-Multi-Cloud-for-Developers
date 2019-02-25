package cz.zcu.kiv.multicloud;

import cz.zcu.kiv.multicloud.utils.AccountManager;
import cz.zcu.kiv.multicloud.utils.CloudManager;
import cz.zcu.kiv.multicloud.utils.CredentialStore;

/**
 * cz.zcu.kiv.multicloud/MultiCloudSettings.java			<br /><br />
 *
 * Class for accumulating custom settings for the MultiCloud library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class MultiCloudSettings {

	/** Cloud settings manager. */
	private CloudManager cloudManager;
	/** Credential store. */
	private CredentialStore credentialStore;
	/** User account manager. */
	private AccountManager accountManager;

	/**
	 * Ctor.
	 */
	public MultiCloudSettings() {
		cloudManager = null;
		credentialStore = null;
		accountManager = null;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.AccountManager} used.
	 * @return User account manager.
	 */
	public AccountManager getAccountManager() {
		return accountManager;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.CloudManager} used.
	 * @return Cloud settings manager.
	 */
	public CloudManager getCloudManager() {
		return cloudManager;
	}

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} used.
	 * @return Credential store.
	 */
	public CredentialStore getCredentialStore() {
		return credentialStore;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.AccountManager} to be used.
	 * @param accountManager User account manager.
	 */
	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.CloudManager} to be used.
	 * @param cloudManager Cloud manager.
	 */
	public void setCloudManager(CloudManager cloudManager) {
		this.cloudManager = cloudManager;
	}

	/**
	 * Sets the {@link cz.zcu.kiv.multicloud.utils.CredentialStore} to be used.
	 * @param store Credential store.
	 */
	public void setCredentialStore(CredentialStore store) {
		this.credentialStore = store;
	}

}
