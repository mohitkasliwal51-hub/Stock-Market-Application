package com.sankalp.walletservice.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private Integer userId;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal balance;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal reservedBalance = BigDecimal.ZERO;

	@Column(nullable = false)
	private String currency = "INR";

	@Column(nullable = false)
	private Timestamp createdAt;

	@Column(nullable = false)
	private Timestamp lastUpdated;

	@PrePersist
	void onCreate() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		this.createdAt = now;
		this.lastUpdated = now;
	}

	@PreUpdate
	void onUpdate() {
		this.lastUpdated = new Timestamp(System.currentTimeMillis());
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

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public BigDecimal getReservedBalance() {
		return reservedBalance;
	}

	public void setReservedBalance(BigDecimal reservedBalance) {
		this.reservedBalance = reservedBalance;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}
}
