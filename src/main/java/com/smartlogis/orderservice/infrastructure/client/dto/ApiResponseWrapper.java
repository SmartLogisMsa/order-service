package com.smartlogis.orderservice.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {
	private boolean success;
	private String messageCode;
	private String message;
	private T data;
}
