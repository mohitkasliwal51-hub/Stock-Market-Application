package com.sankalp.orderservice.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_order_execution")
public class StockOrderExecution {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private StockOrder order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderEventType eventType;

	@Column(precision = 19, scale = 2)
	private BigDecimal marketPrice;

	@Column(precision = 19, scale = 2)
	private BigDecimal referencePrice;

	@Column(nullable = false)
	private String note;

	@Column(nullable = false)
	private Timestamp eventAt;

	@PrePersist
	void onCreate() {
		this.eventAt = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public StockOrder getOrder() {
		return order;
	}

	public void setOrder(StockOrder order) {
		this.order = order;
	}

	public OrderEventType getEventType() {
		return eventType;
	}

	public void setEventType(OrderEventType eventType) {
		this.eventType = eventType;
	}

	public BigDecimal getMarketPrice() {
		return marketPrice;
	}

	public void setMarketPrice(BigDecimal marketPrice) {
		this.marketPrice = marketPrice;
	}

	public BigDecimal getReferencePrice() {
		return referencePrice;
	}

	public void setReferencePrice(BigDecimal referencePrice) {
		this.referencePrice = referencePrice;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Timestamp getEventAt() {
		return eventAt;
	}
}