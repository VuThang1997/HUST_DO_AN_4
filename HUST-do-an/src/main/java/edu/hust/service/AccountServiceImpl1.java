package edu.hust.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import edu.hust.enumData.AccountRole;
import edu.hust.enumData.AccountStatus;
import edu.hust.model.Account;
import edu.hust.model.User;
import edu.hust.repository.AccountRepository;

@Service
@Qualifier("AccountServiceImpl1")
public class AccountServiceImpl1 implements AccountService {

	private AccountRepository accountRepository;

	@Autowired
	public AccountServiceImpl1(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public Account findAccountByEmailAndPassword(String email, String password) {
		Optional<Account> account = this.accountRepository.findByEmailAndPassword(email, password);
		if (account.isPresent()) {
			return account.get();
		}
		return null;
	}

	@Override
	public Account checkEmailIsUsed(String email) {
		Optional<Account> account = this.accountRepository.findByEmail(email);
		return account.isPresent() ? account.get() : null;
	}

	@Override
	public void saveAccount(Account account) {
		this.accountRepository.save(account);
		return;
	}

	@Override
	public boolean deactivateAccount(String email, String password, int role) {
		Optional<Account> account = this.accountRepository.findByEmailAndPassword(email, password);
		if (account.isPresent()) {
			Account target = account.get();
			
			//only admin has full right to disable all types of account;
			//student and teacher just can only disable their own type of account
			if (role != AccountRole.ADMIN.getValue() && role != target.getRole()) {
				return false;
			}
			
			target.setIsActive(AccountStatus.INACTIVE.getValue());
			this.accountRepository.save(target);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean activateAccount(String email, String password, int role) {
		Optional<Account> account = this.accountRepository.findByEmailAndPassword(email, password);
		if (account.isPresent()) {
			Account target = account.get();
			
			//only admin has full right to disable all types of account;
			//student and teacher just can only disable their own type of account
			if (role != AccountRole.ADMIN.getValue() && role != target.getRole()) {
				return false;
			}
			
			target.setIsActive(AccountStatus.ACTIVE.getValue());
			this.accountRepository.save(target);
			return true;
		}
		return false;
	}

	@Override
	public Account updateAccountInfo(Account account) {
		Optional<Account> oldInfo = this.accountRepository.findById(account.getId());
		if (oldInfo.isEmpty()) {
			return null;
		}

		this.accountRepository.save(account);
		return account;
	}

	@Override
	public void addUserInfo(User user) {
		String userInfo = createUserInfoString(user);

		Account account = this.accountRepository.findById(user.getId()).get();
		account.setUserInfo(userInfo);
		this.accountRepository.save(account);
		return;
	}

	@Override
	public Account findAccountByID(int id) {
		Optional<Account> account = this.accountRepository.findById(id);
		if (account.isEmpty()) {
			return null;
		}

		return account.get();
	}

	@Override
	public boolean updateUserInfo(User user) {
		Optional<Account> oldInfo = this.accountRepository.findById(user.getId());

		if (oldInfo.isEmpty()) {
			return false;
		}

		Account account = oldInfo.get();
		String userInfo = createUserInfoString(user);
		account.setUserInfo(userInfo);
		this.accountRepository.save(account);
		return true;
	}

	private String createUserInfoString(User user) {
		String userInfo = null;

		// userInfo has format: "fullName+address+phone+birthDay"
		userInfo = user.getFullName() + "+";
		userInfo += user.getAddress() + "+";
		userInfo += user.getPhone() + "+";
		userInfo += user.getBirthDay();
		
		System.out.println("\n\nUser info = " + userInfo);
		
		return userInfo;
	}

}
