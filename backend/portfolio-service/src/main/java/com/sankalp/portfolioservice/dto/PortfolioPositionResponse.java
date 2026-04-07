package com.sankalp.portfolioservice.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record PortfolioPositionResponse(
		Integer id,
		Integer stockId,
		Integer quantity,
		BigDecimal averageBuyPrice,
		Timestamp createdAt
) {
}
