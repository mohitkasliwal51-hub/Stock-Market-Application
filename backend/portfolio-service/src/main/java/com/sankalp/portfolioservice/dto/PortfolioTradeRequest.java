package com.sankalp.portfolioservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PortfolioTradeRequest {

	@NotNull
	@Min(1)
	private Integer stockId;

	@NotNull
	@Min(1)
	private Integer quantity;

	@NotNull
	@DecimalMin("0.01")
	private BigDecimal executionPrice;

	@NotNull
	private TradeSide side;

	@NotBlank
	private String orderRef;

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

	public BigDecimal getExecutionPrice() {
		return executionPrice;
	}

	public void setExecutionPrice(BigDecimal executionPrice) {
		this.executionPrice = executionPrice;
	}

	public TradeSide getSide() {
		return side;
	}

	public void setSide(TradeSide side) {
		this.side = side;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}
}
