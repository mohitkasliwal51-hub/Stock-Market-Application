package com.sankalp.orderservice.dto;

import java.time.Instant;

public record MarketPriceResponse(
		int stockId,
		String stockCode,
		Integer exchangeId,
		double currentPrice,
		double openingPrice,
		double dayChange,
		Instant lastUpdated
) {
}