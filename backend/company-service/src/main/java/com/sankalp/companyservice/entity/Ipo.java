package com.sankalp.companyservice.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ipo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name="price_per_share")
	private double pricePerShare;
	
	@Column(name="total_shares")
	private int totalShares;
	
	@Column(name="open_datetime")
	private Timestamp dateTime;
	
	private String remarks;
	
	@OneToOne
	private Company company;
	
	@OneToOne
	private StockExchange stockExchange;

	public Ipo(int id, double pricePerShare, int totalShares, Timestamp dateTime, String remarks, Company company,
			StockExchange stockExchange) {
		super();
		this.id = id;
		this.pricePerShare = pricePerShare;
		this.totalShares = totalShares;
		this.dateTime = dateTime;
		this.remarks = remarks;
		this.company = company;
		this.stockExchange = stockExchange;
	}
	
}
