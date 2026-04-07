package com.sankalp.portfolioservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddPositionRequest {

	@NotNull
	@Min(1)
	private Integer stockId;

	@NotNull
	@Min(1)
	private Integer quantity;

	@NotNull
	@DecimalMin("0.01")
	private BigDecimal averageBuyPrice;

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

	public BigDecimal getAverageBuyPrice() {
		return averageBuyPrice;
	}

	public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
		this.averageBuyPrice = averageBuyPrice;
	}
}
