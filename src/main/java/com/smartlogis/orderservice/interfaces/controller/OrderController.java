package com.smartlogis.orderservice.interfaces.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.orderservice.application.service.OrderService;
import com.smartlogis.orderservice.interfaces.dto.request.CreateOrderRequest;
import com.smartlogis.orderservice.interfaces.dto.response.OrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MASTER', 'COMPANY_MANAGER')")
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
		OrderResponse response = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successWithDataOnly(response));
	}

	@DeleteMapping("/{orderId}")
	@PreAuthorize("hasAnyRole('MASTER', 'COMPANY_MANAGER')")
	public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID orderId) {
		OrderResponse response = orderService.cancelOrder(orderId);
		return ResponseEntity.ok(ApiResponse.successWithDataOnly(response));
	}
}
