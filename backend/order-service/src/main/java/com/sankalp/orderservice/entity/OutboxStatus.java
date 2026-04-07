package com.sankalp.orderservice.entity;

public enum OutboxStatus {
	PENDING,
	PROCESSING,
	PROCESSED,
	FAILED
}