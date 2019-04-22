package edu.hust.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.hust.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer>{
	
	Optional<Account> findByEmailAndPassword(String email, String password);
	
	List<Account> findAll();

	List<Account> findByRole(int role);

	Optional<Account> findByEmail(String email);
	
}
