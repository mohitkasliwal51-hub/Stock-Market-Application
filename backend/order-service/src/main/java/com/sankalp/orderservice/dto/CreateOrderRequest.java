package com.sankalp.orderservice.dto;

import java.math.BigDecimal;

import com.sankalp.orderservice.entity.OrderSide;
import com.sankalp.orderservice.entity.OrderType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

	@NotNull
	@Min(1)
	private Integer userId;

	@NotNull
	@Min(1)
	private Integer portfolioId;

	@NotNull
	@Min(1)
	private Integer stockId;

	@NotNull
	@Min(1)
	private Integer quantity;

	@NotNull
	private OrderType orderType;

	@NotNull
	private OrderSide side;

	@NotBlank
	private String idempotencyKey;

	@DecimalMin("0.01")
	private BigDecimal orderPrice;

	@DecimalMin("0.01")
	private BigDecimal triggerPrice;

	@DecimalMin("0.01")
	private BigDecimal trailAmount;

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(Integer portfolioId) {
		this.portfolioId = portfolioId;
	}

	public Integer getStockId() {
		return stockId;
	}

	public void setStockId(Integer stockId) {
		this.stockId = stockId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public OrderSide getSide() {
		return side;
	}

	public void setSide(OrderSide side) {
		this.side = side;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public BigDecimal getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(BigDecimal orderPrice) {
		this.orderPrice = orderPrice;
	}

	public BigDecimal getTriggerPrice() {
		return triggerPrice;
	}

	public void setTriggerPrice(BigDecimal triggerPrice) {
		this.triggerPrice = triggerPrice;
	}

	public BigDecimal getTrailAmount() {
		return trailAmount;
	}

	public void setTrailAmount(BigDecimal trailAmount) {
		this.trailAmount = trailAmount;
	}
}
