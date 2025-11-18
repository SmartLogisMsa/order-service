package com.smartlogis.orderservice.domain.order.exception;

import com.smartlogis.common.exception.AbstractException;

public class InsufficientInventoryException extends AbstractException {
	public InsufficientInventoryException(OrderMessageCode messageCode) {
		super(messageCode);
	}

	public InsufficientInventoryException(OrderMessageCode messageCode, Object... messageArguments) {
		super(messageCode, messageArguments);
	}
}
