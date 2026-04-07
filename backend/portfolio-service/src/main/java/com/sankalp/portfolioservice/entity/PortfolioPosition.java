package com.sankalp.portfolioservice.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "portfolio_position")
public class PortfolioPosition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "portfolio_id", nullable = false)
	private Portfolio portfolio;

	@Column(nullable = false)
	private Integer stockId;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal averageBuyPrice;

	@Column(nullable = false)
	private Timestamp createdAt;

	@PrePersist
	void onCreate() {
		this.createdAt = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public Portfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(Portfolio portfolio) {
		this.portfolio = portfolio;
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

	public BigDecimal getAverageBuyPrice() {
		return averageBuyPrice;
	}

	public void setAverageBuyPrice(BigDecimal averageBuyPrice) {
		this.averageBuyPrice = averageBuyPrice;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}
}
