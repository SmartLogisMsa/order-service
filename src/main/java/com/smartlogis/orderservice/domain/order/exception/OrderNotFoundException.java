package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class OrderNotFoundException extends AbstractException {
	public OrderNotFoundException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public OrderNotFoundException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
