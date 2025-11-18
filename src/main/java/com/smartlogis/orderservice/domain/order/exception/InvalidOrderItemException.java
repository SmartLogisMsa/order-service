package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class InvalidOrderItemException extends AbstractException {
	public InvalidOrderItemException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public InvalidOrderItemException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
