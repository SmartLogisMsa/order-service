package com.smartlogis.orderservice.domain.exception;

import com.smartlogis.common.exception.AbstractException;

public class OrderCannotBeCanceledException extends AbstractException {
	public OrderCannotBeCanceledException(OrderMessageCode messageCode) {
		super(messageCode);
	}
}
