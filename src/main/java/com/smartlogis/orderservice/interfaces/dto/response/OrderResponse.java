package com.smartlogis.orderservice.interfaces.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.smartlogis.orderservice.domain.entity.Order;
import com.smartlogis.orderservice.domain.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
	private UUID id;
	private OrderStatus status;
	private UUID receiptCompanyId;
	private UUID deliveryId;
	private String requestDetails;
	private List<OrderItemResponse> orderItems;
	private LocalDateTime createdAt;
	private String createdBy;
	private LocalDateTime updatedAt;
	private String updatedBy;

	public static OrderResponse from(Order order) {
		return OrderResponse.builder()
			.id(order.getId())
			.status(order.getStatus())
			.receiptCompanyId(order.getReceiptCompanyId())
			.deliveryId(order.getDeliveryId())
			.requestDetails(order.getRequestDetails())
			.orderItems(order.getOrderItems().stream().map(OrderItemResponse::from).collect(Collectors.toList()))
			.createdAt(order.getCreatedAt())
			.createdBy(order.getCreatedBy())
			.updatedAt(order.getUpdatedAt())
			.updatedBy(order.getUpdatedBy())
			.build();
	}
}
