package edu.hust.service;

import edu.hust.model.Account;
import edu.hust.model.User;

/**
 * @author BePro
 *
 */
public interface AccountService {
	
	/**
	 * @param email 
	 * @param password
	 * @return an Account object if login success;
	 */
	Account findAccountByEmailAndPassword(String email, String password);

	
	/**
	 * @param email
	 * @return true if this email has already been used by another account
	 */
	Account checkEmailIsUsed(String email);
	
	/**
	 * @param account - New/Update account need to be saved
	 */
	void saveAccount(Account account);
	
	void addUserInfo(User user);
	
	boolean updateUserInfo(User user);
	
	Account findAccountByID(int id);
	
	boolean deactivateAccount(String email, String password, int role);
	
	boolean activateAccount(String email, String password, int role);

	Account updateAccountInfo(Account account);

	
}
