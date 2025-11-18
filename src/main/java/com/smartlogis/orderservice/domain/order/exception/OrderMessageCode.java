package com.smartlogis.orderservice.domain.order.exception;

import org.springframework.http.HttpStatus;

import com.smartlogis.common.exception.MessageCode;

public enum OrderMessageCode implements MessageCode {
	// Order 관련
	ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND),
	ORDER_CANCEL_FAILED("ORDER_CANCEL_FAILED", HttpStatus.BAD_REQUEST),
	ORDER_INVALID_STATUS("ORDER_INVALID_STATUS", HttpStatus.BAD_REQUEST),

	// OrderItem 관련
	ORDER_ITEM_NOT_FOUND("ORDER_ITEM_NOT_FOUND", HttpStatus.NOT_FOUND),
	ORDER_ITEM_INVALID("ORDER_ITEM_INVALID", HttpStatus.BAD_REQUEST),

	// 유효성 검사
	ORDER_ITEMS_REQUIRED("ORDER_ITEMS_REQUIRED", HttpStatus.BAD_REQUEST),
	ORDER_INVALID_QUANTITY("ORDER_INVALID_QUANTITY", HttpStatus.BAD_REQUEST),

	// 재고 관련
	ORDER_INSUFFICIENT_INVENTORY("ORDER_INSUFFICIENT_INVENTORY", HttpStatus.BAD_REQUEST);

	private final String code;
	private final HttpStatus status;

	OrderMessageCode(String code, HttpStatus status) {
		this.code = code;
		this.status = status;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
