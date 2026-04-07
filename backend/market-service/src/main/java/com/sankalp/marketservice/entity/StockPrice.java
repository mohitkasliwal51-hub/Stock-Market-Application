package com.sankalp.marketservice.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_price")
public class StockPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private double price;

	private Timestamp timestamp;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;

	public StockPrice() {
	}

	public StockPrice(double price, Timestamp timestamp, Stock stock) {
		this.price = price;
		this.timestamp = timestamp;
		this.stock = stock;
	}

	public int getId() {
		return id;
	}

	public double getPrice() {
		return price;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public Stock getStock() {
		return stock;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}
}
