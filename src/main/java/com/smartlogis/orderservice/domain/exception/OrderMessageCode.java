package com.smartlogis.orderservice.domain.exception;

import org.springframework.http.HttpStatus;

import com.smartlogis.common.exception.MessageCode;

public enum OrderMessageCode implements MessageCode {
	// Order 관련
	ORDER_NOT_FOUND("ORDER.NOT_FOUND", HttpStatus.NOT_FOUND),
	ORDER_CANCEL_FAILED("ORDER.CANCEL_FAILED", HttpStatus.BAD_REQUEST),
	ORDER_INVALID_STATUS("ORDER.INVALID_STATUS", HttpStatus.BAD_REQUEST),

	// OrderItem 관련
	ORDER_ITEM_NOT_FOUND("ORDER.ITEM_NOT_FOUND", HttpStatus.NOT_FOUND),
	ORDER_ITEM_INVALID("ORDER.ITEM_INVALID", HttpStatus.BAD_REQUEST),

	// 유효성 검사
	ORDER_ITEMS_REQUIRED("ORDER.ITEMS_REQUIRED", HttpStatus.BAD_REQUEST),
	ORDER_INVALID_QUANTITY("ORDER.INVALID_QUANTITY", HttpStatus.BAD_REQUEST),

	// 재고 관련
	ORDER_INSUFFICIENT_INVENTORY("ORDER.INSUFFICIENT_INVENTORY", HttpStatus.BAD_REQUEST);

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
