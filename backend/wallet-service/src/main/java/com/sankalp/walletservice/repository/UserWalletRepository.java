package com.sankalp.walletservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.walletservice.entity.UserWallet;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Integer> {

	Optional<UserWallet> findByUserId(Integer userId);
}
