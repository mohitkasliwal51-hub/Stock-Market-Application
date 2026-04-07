package com.sankalp.marketservice.dto;

import java.time.Instant;

public record LivePriceResponse(
		int stockId,
		String stockCode,
		Integer exchangeId,
		double currentPrice,
		double openingPrice,
		double dayChange,
		Instant lastUpdated
) {}
