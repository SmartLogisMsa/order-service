package com.smartlogis.orderservice.domain.event;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.smartlogis.orderservice.domain.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreatedEvent {
	private UUID orderId;
	private UUID receiptCompanyId;
	private List<OrderItemDetail> orderItems;
	private String requestDetails;
	private LocalDateTime createdAt;
	private String createdBy;

	public static OrderCreatedEvent from(Order order) {
		return OrderCreatedEvent.builder()
			.orderId(order.getId())
			.receiptCompanyId(order.getReceiptCompanyId())
			.orderItems(order.getOrderItems().stream()
				.map(item -> OrderItemDetail.builder()
					.productId(item.getProductId())
					.quantity(item.getQuantity())
					.build())
				.toList())
			.requestDetails(order.getRequestDetails())
			.createdAt(order.getCreatedAt())
			.createdBy(order.getCreatedBy())
			.build();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class OrderItemDetail implements Serializable {
		private static final long serialVersionUID = 1L;
		private UUID productId;
		private Integer quantity;
	}
}
