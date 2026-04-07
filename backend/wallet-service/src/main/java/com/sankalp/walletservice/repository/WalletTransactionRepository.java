package com.sankalp.walletservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sankalp.walletservice.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Integer> {

	List<WalletTransaction> findByWalletIdOrderByTransactionDateDesc(Integer walletId);
}
