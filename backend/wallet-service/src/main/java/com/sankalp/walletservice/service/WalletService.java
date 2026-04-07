package com.sankalp.walletservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sankalp.walletservice.dto.CreateWalletRequest;
import com.sankalp.walletservice.dto.WalletOperationRequest;
import com.sankalp.walletservice.dto.WalletResponse;
import com.sankalp.walletservice.dto.WalletTransactionResponse;
import com.sankalp.walletservice.entity.UserWallet;
import com.sankalp.walletservice.entity.WalletTransaction;
import com.sankalp.walletservice.entity.WalletTransactionStatus;
import com.sankalp.walletservice.entity.WalletTransactionType;
import com.sankalp.walletservice.repository.UserWalletRepository;
import com.sankalp.walletservice.repository.WalletTransactionRepository;

@Service
public class WalletService {

	private final UserWalletRepository userWalletRepository;
	private final WalletTransactionRepository walletTransactionRepository;

	public WalletService(UserWalletRepository userWalletRepository,
			WalletTransactionRepository walletTransactionRepository) {
		this.userWalletRepository = userWalletRepository;
		this.walletTransactionRepository = walletTransactionRepository;
	}

	@Transactional
	public WalletResponse createWallet(CreateWalletRequest request) {
		if (userWalletRepository.findByUserId(request.getUserId()).isPresent()) {
			throw new IllegalArgumentException("Wallet already exists for user: " + request.getUserId());
		}

		UserWallet wallet = new UserWallet();
		wallet.setUserId(request.getUserId());
		wallet.setCurrency(request.getCurrency() == null || request.getCurrency().isBlank() ? "INR" : request.getCurrency());
		wallet.setBalance(request.getInitialBalance());

		UserWallet saved = userWalletRepository.save(wallet);
		if (request.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
			recordTransaction(saved, WalletTransactionType.DEPOSIT, request.getInitialBalance(), "Initial wallet funding",
					"INIT-" + saved.getUserId(), WalletTransactionStatus.COMPLETED);
		}

		return toWalletResponse(saved);
	}

	@Transactional(readOnly = true)
	public WalletResponse getWalletByUser(Integer userId) {
		return toWalletResponse(fetchWallet(userId));
	}

	@Transactional
	public WalletResponse deposit(Integer userId, WalletOperationRequest request) {
		UserWallet wallet = fetchWallet(userId);
		wallet.setBalance(wallet.getBalance().add(request.getAmount()));
		UserWallet saved = userWalletRepository.save(wallet);

		recordTransaction(saved, WalletTransactionType.DEPOSIT, request.getAmount(), request.getDescription(),
				request.getReferenceId(), WalletTransactionStatus.COMPLETED);

		return toWalletResponse(saved);
	}

	@Transactional
	public WalletResponse withdraw(Integer userId, WalletOperationRequest request) {
		UserWallet wallet = fetchWallet(userId);
		if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
			recordTransaction(wallet, WalletTransactionType.WITHDRAWAL, request.getAmount(), request.getDescription(),
					request.getReferenceId(), WalletTransactionStatus.FAILED);
			throw new IllegalArgumentException("Insufficient balance for withdrawal");
		}

		wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
		UserWallet saved = userWalletRepository.save(wallet);

		recordTransaction(saved, WalletTransactionType.WITHDRAWAL, request.getAmount(), request.getDescription(),
				request.getReferenceId(), WalletTransactionStatus.COMPLETED);

		return toWalletResponse(saved);
	}

	@Transactional(readOnly = true)
	public List<WalletTransactionResponse> getTransactions(Integer userId) {
		UserWallet wallet = fetchWallet(userId);
		return walletTransactionRepository.findByWalletIdOrderByTransactionDateDesc(wallet.getId()).stream()
				.map(this::toTransactionResponse)
				.toList();
	}

	private UserWallet fetchWallet(Integer userId) {
		return userWalletRepository.findByUserId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Wallet not found for user: " + userId));
	}

	private void recordTransaction(UserWallet wallet, WalletTransactionType type, BigDecimal amount,
			String description, String referenceId, WalletTransactionStatus status) {
		WalletTransaction transaction = new WalletTransaction();
		transaction.setWallet(wallet);
		transaction.setTransactionType(type);
		transaction.setAmount(amount);
		transaction.setDescription(description);
		transaction.setReferenceId(referenceId);
		transaction.setStatus(status);
		walletTransactionRepository.save(transaction);
	}

	private WalletResponse toWalletResponse(UserWallet wallet) {
		return new WalletResponse(
				wallet.getId(),
				wallet.getUserId(),
				wallet.getBalance(),
				wallet.getCurrency(),
				wallet.getCreatedAt(),
				wallet.getLastUpdated());
	}

	private WalletTransactionResponse toTransactionResponse(WalletTransaction transaction) {
		return new WalletTransactionResponse(
				transaction.getId(),
				transaction.getTransactionType(),
				transaction.getAmount(),
				transaction.getStatus(),
				transaction.getDescription(),
				transaction.getReferenceId(),
				transaction.getTransactionDate());
	}
}
