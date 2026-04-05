package com.sankalp.userservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.userservice.entity.UserAccount;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
	Optional<UserAccount> findByUsername(String username);
	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
}
