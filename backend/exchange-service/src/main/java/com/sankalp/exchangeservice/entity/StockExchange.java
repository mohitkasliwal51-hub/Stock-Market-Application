package com.sankalp.exchangeservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="STOCK_EXCHANGE")
@Getter
@Setter
@NoArgsConstructor
public class StockExchange {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "BRIEF")
	private String brief;
	
	@Column(name = "REMARKS")
	private String remarks;
	
	@OneToOne(cascade = {CascadeType.ALL})
	private Address address;

	public StockExchange(int id, String name, String brief, String remarks, Address addressId) {
		super();
		this.id = id;
		this.name = name;
		this.brief = brief;
		this.remarks = remarks;
		this.address = addressId;
	}
	
}
