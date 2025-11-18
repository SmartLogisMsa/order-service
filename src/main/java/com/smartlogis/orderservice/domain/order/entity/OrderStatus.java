package com.smartlogis.orderservice.domain.order.entity;

public enum OrderStatus {
	CREATED("접수됨"),
	CANCELED("취소됨");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
