package com.smartlogis.orderservice.domain.entity;

public enum OrderStatus {
	PENDING("접수됨"),
	CANCELED("취소됨"),
	SHIPPED("배송중"),
	DELIVERED("배송완료");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
