package com.sankalp.orderservice.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class ExecuteOrderRequest {

	@NotNull
	@DecimalMin("0.01")
	private BigDecimal executionPrice;

	public BigDecimal getExecutionPrice() {
		return executionPrice;
	}

	public void setExecutionPrice(BigDecimal executionPrice) {
		this.executionPrice = executionPrice;
	}
}
