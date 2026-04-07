package com.sankalp.orderservice.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_outbox_event")
public class OrderOutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 60)
	private String aggregateType;

	@Column(nullable = false)
	private Integer aggregateId;

	@Column(nullable = false, length = 80)
	private String eventType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private OutboxStatus status = OutboxStatus.PENDING;

	@Column(nullable = false)
	private int retryCount = 0;

	@Column(nullable = false)
	private Timestamp nextAttemptAt;

	@Column(columnDefinition = "TEXT")
	private String lastError;

	@Column(nullable = false)
	private Timestamp createdAt;

	@Column(nullable = false)
	private Timestamp updatedAt;

	@PrePersist
	void onCreate() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		this.createdAt = now;
		this.updatedAt = now;
		if (this.nextAttemptAt == null) {
			this.nextAttemptAt = now;
		}
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = new Timestamp(System.currentTimeMillis());
	}

	public Long getId() {
		return id;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public Integer getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(Integer aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public OutboxStatus getStatus() {
		return status;
	}

	public void setStatus(OutboxStatus status) {
		this.status = status;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public Timestamp getNextAttemptAt() {
		return nextAttemptAt;
	}

	public void setNextAttemptAt(Timestamp nextAttemptAt) {
		this.nextAttemptAt = nextAttemptAt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
}