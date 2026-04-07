package com.sankalp.walletservice.dto;

import java.math.BigDecimal;

public record WalletBalanceHookResponse(
		Integer userId,
		BigDecimal balance,
		BigDecimal reservedBalance,
		BigDecimal availableBalance,
		String operation,
		String referenceId
) {
}
