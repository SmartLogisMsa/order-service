package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class OrderCancelFailedException extends AbstractException {
	public OrderCancelFailedException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public OrderCancelFailedException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
