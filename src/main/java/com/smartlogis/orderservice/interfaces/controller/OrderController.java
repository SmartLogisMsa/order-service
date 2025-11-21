package com.smartlogis.orderservice.interfaces.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartlogis.common.presentation.ApiResponse;
import com.smartlogis.common.presentation.dto.PageRequest;
import com.smartlogis.common.presentation.dto.PageResponse;
import com.smartlogis.orderservice.application.service.OrderService;
import com.smartlogis.orderservice.interfaces.dto.request.CreateOrderRequest;
import com.smartlogis.orderservice.interfaces.dto.response.OrderResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@PreAuthorize("hasAnyRole('MASTER', 'COMPANY_MANAGER')")
	@Operation(
		summary = "주문 생성",
		description = "새로운 주문을 생성합니다. 재고 확인 후 주문이 생성되며, OrderCreatedEvent가 발행됩니다."
	)
	public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
		@Valid @RequestBody CreateOrderRequest request) {
		OrderResponse response = orderService.createOrder(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.successWithDataOnly(response));
	}

	@PatchMapping("/cancel/{orderId}")
	@PreAuthorize("hasAnyRole('MASTER', 'COMPANY_MANAGER')")
	@Operation(
		summary = "주문 취소",
		description = "PENDING 상태의 주문만 취소 가능합니다. 취소 시 OrderCanceledEvent가 발행됩니다."
	)
	public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
		@PathVariable
		@Parameter(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
		UUID orderId) {
		OrderResponse response = orderService.cancelOrder(orderId);
		return ResponseEntity.ok(ApiResponse.successWithDataOnly(response));
	}

	@GetMapping("/{orderId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "주문 단건 조회",
		description = "삭제되지 않은 주문을 ID로 조회합니다."
	)
	public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
		@PathVariable
		@Parameter(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
		UUID orderId) {
		OrderResponse response = orderService.getOrder(orderId);
		return ResponseEntity.ok(ApiResponse.successWithDataOnly(response));
	}

	@GetMapping("/company/{receiptCompanyId}")
	@PreAuthorize("isAuthenticated()")
	@Operation(
		summary = "업체별 주문 목록 조회",
		description = "특정 업체의 주문 목록을 페이지네이션하여 조회합니다."
	)
	public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByCompany(
		@PathVariable
		@Parameter(description = "업체 ID", example = "550e8400-e29b-41d4-a716-446655440000")
		UUID receiptCompanyId,
		@ModelAttribute
		@Parameter(description = "페이지네이션 요청")
		PageRequest pageRequest) {
		PageResponse<OrderResponse> response = orderService.getOrdersByCompany(receiptCompanyId, pageRequest);
		return ResponseEntity.ok(ApiResponse.successWithDataOnly(response));
	}

	@DeleteMapping("/{orderId}")
	@PreAuthorize("hasAnyRole('MASTER')")
	@Operation(
		summary = "주문 삭제 (논리적 삭제)",
		description = "주문을 논리적으로 삭제합니다. 삭제된 주문은 조회되지 않습니다."
	)
	public ResponseEntity<ApiResponse<Void>> deleteOrder(
		@PathVariable
		@Parameter(description = "주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
		UUID orderId) {
		orderService.deleteOrder(orderId);
		return ResponseEntity.ok(ApiResponse.successWithDataOnly(null));
	}
}
