package com.smartlogis.orderservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckRequest;
import com.smartlogis.orderservice.infrastructure.client.dto.InventoryCheckResponse;

@FeignClient(name = "product-service")
public interface ProductServiceClient {

	@PostMapping("/v1/products/inventory/check")
	InventoryCheckResponse checkInventoriesBatch(@RequestBody InventoryCheckRequest request);
}
