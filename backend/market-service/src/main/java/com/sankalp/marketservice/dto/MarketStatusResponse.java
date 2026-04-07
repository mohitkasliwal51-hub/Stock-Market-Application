package com.sankalp.marketservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record MarketStatusResponse(
		boolean marketOpen,
		String timezone,
		LocalDate tradingDate,
		LocalDateTime currentTime,
		LocalTime opensAt,
		LocalTime closesAt,
		String reason,
		LocalDateTime nextOpenAt
) {}
