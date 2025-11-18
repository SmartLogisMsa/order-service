package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class OrderItemNotFoundException extends AbstractException {
	public OrderItemNotFoundException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public OrderItemNotFoundException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
