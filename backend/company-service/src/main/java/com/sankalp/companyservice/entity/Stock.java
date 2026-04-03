package com.sankalp.companyservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="stock_code")
	private String stockCode;
	
	@ManyToOne
	private Company company;
	
	@ManyToOne
	private StockExchange stockExchange;

	public Stock(int id, String stockCode, Company company, StockExchange stockExchange) {
		super();
		this.id = id;
		this.stockCode = stockCode;
		this.company = company;
		this.stockExchange = stockExchange;
	}
	
}
