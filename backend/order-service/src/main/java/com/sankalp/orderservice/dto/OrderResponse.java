package com.sankalp.orderservice.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.sankalp.orderservice.entity.OrderSide;
import com.sankalp.orderservice.entity.OrderStatus;
import com.sankalp.orderservice.entity.OrderType;

public record OrderResponse(
		Integer id,
		Integer userId,
		Integer portfolioId,
		Integer stockId,
		Integer quantity,
		OrderType orderType,
		OrderSide side,
		OrderStatus status,
		BigDecimal orderPrice,
		BigDecimal triggerPrice,
		BigDecimal reservedAmount,
		BigDecimal executedPrice,
		Timestamp createdAt,
		Timestamp executedAt
) {
}
