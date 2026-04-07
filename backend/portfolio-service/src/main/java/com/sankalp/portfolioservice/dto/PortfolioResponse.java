package com.sankalp.portfolioservice.dto;

import java.sql.Timestamp;
import java.util.List;

public record PortfolioResponse(
		Integer id,
		Integer userId,
		String portfolioName,
		boolean active,
		Timestamp createdAt,
		Timestamp updatedAt,
		List<PortfolioPositionResponse> positions
) {
}
