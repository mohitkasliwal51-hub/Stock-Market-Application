package com.sankalp.walletservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sankalp.walletservice.dto.CreateWalletRequest;
import com.sankalp.walletservice.dto.OrderWalletRequest;
import com.sankalp.walletservice.dto.WalletBalanceHookResponse;
import com.sankalp.walletservice.dto.WalletOperationRequest;
import com.sankalp.walletservice.dto.WalletResponse;
import com.sankalp.walletservice.dto.WalletTransactionResponse;
import com.sankalp.walletservice.service.WalletService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

	private final WalletService walletService;

	public WalletController(WalletService walletService) {
		this.walletService = walletService;
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("service", "wallet-service", "status", "UP"));
	}

	@PostMapping
	public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
		return ResponseEntity.ok(walletService.createWallet(request));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<WalletResponse> getWallet(@PathVariable Integer userId) {
		return ResponseEntity.ok(walletService.getWalletByUser(userId));
	}

	@PostMapping("/{userId}/deposit")
	public ResponseEntity<WalletResponse> deposit(
			@PathVariable Integer userId,
			@Valid @RequestBody WalletOperationRequest request) {
		return ResponseEntity.ok(walletService.deposit(userId, request));
	}

	@PostMapping("/{userId}/withdraw")
	public ResponseEntity<WalletResponse> withdraw(
			@PathVariable Integer userId,
			@Valid @RequestBody WalletOperationRequest request) {
		return ResponseEntity.ok(walletService.withdraw(userId, request));
	}

	@GetMapping("/{userId}/transactions")
	public ResponseEntity<List<WalletTransactionResponse>> getTransactions(@PathVariable Integer userId) {
		return ResponseEntity.ok(walletService.getTransactions(userId));
	}

	@PostMapping("/hooks/order/reserve")
	public ResponseEntity<WalletBalanceHookResponse> reserveForOrder(@Valid @RequestBody OrderWalletRequest request) {
		return ResponseEntity.ok(walletService.reserveForOrder(request));
	}

	@PostMapping("/hooks/order/release")
	public ResponseEntity<WalletBalanceHookResponse> releaseReservation(@Valid @RequestBody OrderWalletRequest request) {
		return ResponseEntity.ok(walletService.releaseReservation(request));
	}

	@PostMapping("/hooks/order/debit")
	public ResponseEntity<WalletBalanceHookResponse> debitReserved(@Valid @RequestBody OrderWalletRequest request) {
		return ResponseEntity.ok(walletService.debitReserved(request));
	}

	@PostMapping("/hooks/order/credit")
	public ResponseEntity<WalletBalanceHookResponse> creditForOrder(@Valid @RequestBody OrderWalletRequest request) {
		return ResponseEntity.ok(walletService.creditForOrder(request));
	}
}
