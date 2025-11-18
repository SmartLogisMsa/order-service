package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class InvalidOrderStatusException extends AbstractException {
	public InvalidOrderStatusException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public InvalidOrderStatusException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
