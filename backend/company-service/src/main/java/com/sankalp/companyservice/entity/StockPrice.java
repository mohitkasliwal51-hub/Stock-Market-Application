package com.sankalp.companyservice.entity;

import java.sql.Timestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StockPrice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private double price;
	
	private Timestamp timestamp;
	
	@NotNull
	@ManyToOne
	private Stock stock;

	public StockPrice(int id, double price, Timestamp timestamp, Stock stock) {
		super();
		this.id = id;
		this.price = price;
		this.timestamp = timestamp;
		this.stock = stock;
	}
	
}
