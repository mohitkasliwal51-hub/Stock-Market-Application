package com.sankalp.walletservice.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.sankalp.walletservice.entity.WalletTransactionStatus;
import com.sankalp.walletservice.entity.WalletTransactionType;

public record WalletTransactionResponse(
		Integer id,
		WalletTransactionType transactionType,
		BigDecimal amount,
		WalletTransactionStatus status,
		String description,
		String referenceId,
		Timestamp transactionDate
) {
}
