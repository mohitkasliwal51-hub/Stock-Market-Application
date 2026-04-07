package com.sankalp.orderservice.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.sankalp.orderservice.entity.OrderEventType;

public record OrderExecutionResponse(
		Integer id,
		OrderEventType eventType,
		BigDecimal marketPrice,
		BigDecimal referencePrice,
		String note,
		Timestamp eventAt
) {
}
