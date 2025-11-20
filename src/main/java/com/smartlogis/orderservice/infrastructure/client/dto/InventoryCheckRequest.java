package com.smartlogis.orderservice.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCheckRequest {
	private List<InventoryCheckItem> inventoryChecks;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class InventoryCheckItem {
		private UUID productId;
		private Integer requestedQuantity;
	}
}
