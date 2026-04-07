package com.sankalp.orderservice.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_order")
public class StockOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private Integer userId;

	@Column(nullable = false)
	private Integer portfolioId;

	@Column(nullable = false)
	private Integer stockId;

	@Column(nullable = false)
	private Integer quantity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderType orderType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderSide side;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(precision = 19, scale = 2)
	private BigDecimal orderPrice;

	@Column(precision = 19, scale = 2)
	private BigDecimal triggerPrice;

	@Column(precision = 19, scale = 2)
	private BigDecimal executedPrice;

	@Column(precision = 19, scale = 2)
	private BigDecimal reservedAmount;

	@Column(nullable = false)
	private Timestamp createdAt;

	private Timestamp executedAt;

	@PrePersist
	void onCreate() {
		this.createdAt = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

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

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
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

	public BigDecimal getExecutedPrice() {
		return executedPrice;
	}

	public void setExecutedPrice(BigDecimal executedPrice) {
		this.executedPrice = executedPrice;
	}

	public BigDecimal getReservedAmount() {
		return reservedAmount;
	}

	public void setReservedAmount(BigDecimal reservedAmount) {
		this.reservedAmount = reservedAmount;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public Timestamp getExecutedAt() {
		return executedAt;
	}

	public void setExecutedAt(Timestamp executedAt) {
		this.executedAt = executedAt;
	}
}
