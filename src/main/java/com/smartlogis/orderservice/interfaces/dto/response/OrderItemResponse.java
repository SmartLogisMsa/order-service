package com.smartlogis.orderservice.interfaces.dto.response;

import java.util.UUID;

import com.smartlogis.orderservice.domain.entity.OrderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
	private UUID id;
	private UUID productId;
	private Integer quantity;

	public static OrderItemResponse from(OrderItem item) {
		return OrderItemResponse.builder()
			.id(item.getId())
			.productId(item.getProductId())
			.quantity(item.getQuantity())
			.build();
	}
}
