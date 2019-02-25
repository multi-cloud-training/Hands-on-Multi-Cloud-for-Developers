package cz.zcu.kiv.multicloud.utils;

import java.util.Collection;
import java.util.Set;

import cz.zcu.kiv.multicloud.json.AccountSettings;

/**
 * cz.zcu.kiv.multicloud.utils/AccountManager.java			<br /><br />
 *
 * Interface for storing user accounts for different cloud storage services.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface AccountManager {

	/**
	 * Adds the {@link cz.zcu.kiv.multicloud.json.AccountSettings} to local store.
	 * @param settings User settings.
	 */
	void addAccountSettings(AccountSettings settings);

	/**
	 * Returns the {@link cz.zcu.kiv.multicloud.json.AccountSettings} specified by the identifier.
	 * @param accountId User settings identifier.
	 * @return User settings.
	 */
	AccountSettings getAccountSettings(String accountId);

	/**
	 * Returns all the {@link cz.zcu.kiv.multicloud.json.AccountSettings} in the store.
	 * @return All user settings.
	 */
	Collection<AccountSettings> getAllAccountSettings();

	/**
	 * Returns all the identifiers used for the accounts.
	 * @return Set of all identifiers.
	 */
	Set<String> getIdentifiers();

	/**
	 * Removes the specified {@link cz.zcu.kiv.multicloud.json.AccountSettings}.
	 * @param accountId Account settings identifier.
	 */
	void removeAccountSettings(String accountId);

	/**
	 * Saves all the changes in the {@link cz.zcu.kiv.multicloud.json.AccountSettings} store.
	 */
	void saveAccountSettings();

}
