package com.sankalp.walletservice.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record WalletResponse(
		Integer walletId,
		Integer userId,
		BigDecimal balance,
		String currency,
		Timestamp createdAt,
		Timestamp lastUpdated
) {
}
