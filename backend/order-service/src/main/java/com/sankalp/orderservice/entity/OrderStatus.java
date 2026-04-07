package com.sankalp.orderservice.entity;

public enum OrderStatus {
	CREATED,
	OPEN,
	RESERVED,
	TRIGGER_PENDING,
	EXECUTING,
	EXECUTED,
	REJECTED,
	CANCELLED
}
