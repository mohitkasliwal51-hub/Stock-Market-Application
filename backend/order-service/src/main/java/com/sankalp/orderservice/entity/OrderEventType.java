package com.sankalp.orderservice.entity;

public enum OrderEventType {
	CREATED,
	EVALUATED,
	TRIGGERED,
	EXECUTED,
	COMPENSATED,
	CANCELLED,
	REJECTED
}