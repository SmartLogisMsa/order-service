package com.smartlogis.orderservice.domain.event;

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
public class OrderCanceledEvent {
	private UUID orderId;
	private UUID receiptCompanyId;
	private List<OrderItemDetail> orderItems;
	private String requestDetails;
	private LocalDateTime canceledAt;
	private String canceledBy;

	public static OrderCanceledEvent from(Order order) {
		return OrderCanceledEvent.builder()
			.orderId(order.getId())
			.receiptCompanyId(order.getReceiptCompanyId())
			.orderItems(order.getOrderItems().stream()
				.map(item -> OrderItemDetail.builder()
					.productId(item.getProductId())
					.quantity(item.getQuantity())
					.build())
				.toList())
			.requestDetails(order.getRequestDetails())
			.canceledAt(order.getUpdatedAt())
			.canceledBy(order.getUpdatedBy())
			.build();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class OrderItemDetail {
		private UUID productId;
		private Integer quantity;
	}
}
