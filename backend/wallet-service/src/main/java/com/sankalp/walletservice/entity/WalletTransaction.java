package com.sankalp.walletservice.entity;

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
@Table(name = "wallet_transaction")
public class WalletTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "wallet_id", nullable = false)
	private UserWallet wallet;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WalletTransactionType transactionType;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private WalletTransactionStatus status;

	@Column(nullable = false)
	private Timestamp transactionDate;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private String referenceId;

	@PrePersist
	void onCreate() {
		this.transactionDate = new Timestamp(System.currentTimeMillis());
	}

	public Integer getId() {
		return id;
	}

	public UserWallet getWallet() {
		return wallet;
	}

	public void setWallet(UserWallet wallet) {
		this.wallet = wallet;
	}

	public WalletTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(WalletTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public WalletTransactionStatus getStatus() {
		return status;
	}

	public void setStatus(WalletTransactionStatus status) {
		this.status = status;
	}

	public Timestamp getTransactionDate() {
		return transactionDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
}
