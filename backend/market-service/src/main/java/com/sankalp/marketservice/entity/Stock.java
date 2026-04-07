package com.sankalp.marketservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock")
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "stock_code")
	private String stockCode;

	@Column(name = "stock_exchange_id")
	private Integer stockExchangeId;

	public int getId() {
		return id;
	}

	public String getStockCode() {
		return stockCode;
	}

	public Integer getStockExchangeId() {
		return stockExchangeId;
	}
}
