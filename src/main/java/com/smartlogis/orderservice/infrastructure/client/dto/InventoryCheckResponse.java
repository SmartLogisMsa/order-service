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
public class InventoryCheckResponse {
	private List<InventoryCheckResult> results;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class InventoryCheckResult {
		private UUID productId;
		private Boolean available;
	}
}
